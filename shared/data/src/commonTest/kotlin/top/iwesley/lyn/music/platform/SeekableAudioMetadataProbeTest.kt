package top.iwesley.lyn.music.platform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import top.iwesley.lyn.music.core.model.AudioTagSnapshot

class SeekableAudioMetadataProbeTest {

    @Test
    fun `seekable probe expands mp3 head when first read is partial`() {
        val mp3Bytes = buildSeekableId3v23Tag(
            seekableTextFrame("TIT2", "Expanded"),
            seekableUsltFrame("line one\nline two"),
        )
        val reads = mutableListOf<Pair<Long, Int>>()
        var firstRead = true

        val metadata = probeSeekableAudioMetadata(
            relativePath = "Artist/Expanded.mp3",
            sizeBytes = null,
        ) { offset, length ->
            reads += offset to length
            if (firstRead) {
                firstRead = false
                mp3Bytes.copyOfRange(0, 32)
            } else {
                mp3Bytes.copyOfRange(offset.toInt(), (offset + length).toInt().coerceAtMost(mp3Bytes.size))
            }
        }

        assertNotNull(metadata)
        assertEquals("Expanded", metadata.title)
        assertEquals("line one\nline two", metadata.embeddedLyrics)
        assertEquals(0L to RemoteAudioMetadataProbe.HEAD_PROBE_BYTES.toInt(), reads.first())
        assertEquals(0L to mp3Bytes.size, reads.last())
    }

    @Test
    fun `seekable probe skips tail reads when size is unknown`() {
        val headBytes = seekableAtom("ftyp", seekableAscii("M4A "))
        val reads = mutableListOf<Pair<Long, Int>>()

        val metadata = probeSeekableAudioMetadata(
            relativePath = "Artist/Unknown.m4a",
            sizeBytes = null,
        ) { offset, length ->
            reads += offset to length
            headBytes.copyOfRange(0, length.coerceAtMost(headBytes.size))
        }

        assertNull(metadata)
        assertEquals(listOf(0L to RemoteAudioMetadataProbe.HEAD_PROBE_BYTES.toInt()), reads)
    }

    @Test
    fun `seekable probe reads mp4 tail when size is known`() {
        val headBytes = seekableAtom("ftyp", seekableAscii("M4A "))
        val tailBytes = buildSeekableMp4Moov(
            title = "Tail Title",
            lyrics = "tail lyric",
        )
        val sizeBytes = 1_000_000L
        val expectedTailLength = RemoteAudioMetadataProbe.TAIL_PROBE_BYTES.toInt()
        val expectedTailOffset = sizeBytes - expectedTailLength
        val reads = mutableListOf<Pair<Long, Int>>()

        val metadata = probeSeekableAudioMetadata(
            relativePath = "Artist/Known.m4a",
            sizeBytes = sizeBytes,
        ) { offset, length ->
            reads += offset to length
            when (offset) {
                0L -> headBytes.copyOfRange(0, length.coerceAtMost(headBytes.size))
                expectedTailOffset -> tailBytes.copyOfRange(0, length.coerceAtMost(tailBytes.size))
                else -> ByteArray(0)
            }
        }

        assertNotNull(metadata)
        assertEquals("Tail Title", metadata.title)
        assertEquals("tail lyric", metadata.embeddedLyrics)
        assertEquals(2, reads.size)
        assertEquals(0L to RemoteAudioMetadataProbe.HEAD_PROBE_BYTES.toInt(), reads[0])
        assertEquals(expectedTailOffset to expectedTailLength, reads[1])
    }

    @Test
    fun `merge embedded lyrics keeps retriever snapshot unchanged when probe misses`() {
        val snapshot = AudioTagSnapshot(
            title = "Track",
            artistName = "Artist",
            albumTitle = "Album",
            embeddedLyrics = null,
            artworkLocator = "/tmp/art.jpg",
        )

        val merged = snapshot.mergeEmbeddedLyrics(metadata = null)

        assertEquals(snapshot, merged)
        assertNull(merged.embeddedLyrics)
    }

    @Test
    fun `merge embedded lyrics applies normalized probe lyrics`() {
        val snapshot = AudioTagSnapshot(title = "Track")

        val merged = snapshot.mergeEmbeddedLyrics(
            RemoteAudioMetadata(embeddedLyrics = "  [00:01.00]line  "),
        )

        assertEquals("[00:01.00]line", merged.embeddedLyrics)
    }
}

private fun buildSeekableId3v23Tag(vararg frames: ByteArray): ByteArray {
    val payload = frames.fold(ByteArray(0)) { acc, frame -> acc + frame }
    return byteArrayOf(
        'I'.code.toByte(),
        'D'.code.toByte(),
        '3'.code.toByte(),
        3,
        0,
        0,
    ) + seekableSyncSafe(payload.size) + payload
}

private fun seekableTextFrame(id: String, value: String): ByteArray {
    val payload = byteArrayOf(3) + value.encodeToByteArray()
    return id.encodeToByteArray() + seekableBe32(payload.size) + byteArrayOf(0, 0) + payload
}

private fun seekableUsltFrame(lyrics: String): ByteArray {
    val payload = byteArrayOf(3) + seekableAscii("eng") + byteArrayOf(0) + lyrics.encodeToByteArray()
    return seekableAscii("USLT") + seekableBe32(payload.size) + byteArrayOf(0, 0) + payload
}

private fun buildSeekableMp4Moov(
    title: String,
    lyrics: String,
): ByteArray {
    val ilstBody = listOf(
        seekableTextEntry(byteArrayOf(0xA9.toByte(), 'n'.code.toByte(), 'a'.code.toByte(), 'm'.code.toByte()), title),
        seekableTextEntry(byteArrayOf(0xA9.toByte(), 'l'.code.toByte(), 'y'.code.toByte(), 'r'.code.toByte()), lyrics),
    ).fold(ByteArray(0)) { acc, entry -> acc + entry }
    val metaBody = byteArrayOf(0, 0, 0, 0) + seekableAtom("ilst", ilstBody)
    return seekableAtom("moov", seekableAtom("udta", seekableAtom("meta", metaBody)))
}

private fun seekableTextEntry(type: ByteArray, value: String): ByteArray {
    val dataBody = ByteArray(8) + value.encodeToByteArray()
    return seekableAtom(type, seekableAtom("data", dataBody))
}

private fun seekableAtom(type: String, body: ByteArray): ByteArray = seekableAtom(seekableAscii(type), body)

private fun seekableAtom(type: ByteArray, body: ByteArray): ByteArray {
    return seekableBe32(body.size + 8) + type + body
}

private fun seekableAscii(value: String): ByteArray = value.encodeToByteArray()

private fun seekableBe32(value: Int): ByteArray {
    return byteArrayOf(
        ((value ushr 24) and 0xFF).toByte(),
        ((value ushr 16) and 0xFF).toByte(),
        ((value ushr 8) and 0xFF).toByte(),
        (value and 0xFF).toByte(),
    )
}

private fun seekableSyncSafe(value: Int): ByteArray {
    return byteArrayOf(
        ((value ushr 21) and 0x7F).toByte(),
        ((value ushr 14) and 0x7F).toByte(),
        ((value ushr 7) and 0x7F).toByte(),
        (value and 0x7F).toByte(),
    )
}
