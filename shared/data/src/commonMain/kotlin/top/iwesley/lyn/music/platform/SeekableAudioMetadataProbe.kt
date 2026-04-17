package top.iwesley.lyn.music.platform

import top.iwesley.lyn.music.core.model.AudioTagSnapshot

internal fun probeSeekableAudioMetadata(
    relativePath: String,
    sizeBytes: Long?,
    readBytes: (offset: Long, length: Int) -> ByteArray,
): RemoteAudioMetadata? {
    val normalizedSize = sizeBytes?.takeIf { it > 0L }
    val initialHeadBytes = (normalizedSize ?: RemoteAudioMetadataProbe.HEAD_PROBE_BYTES)
        .coerceAtMost(RemoteAudioMetadataProbe.HEAD_PROBE_BYTES)
        .coerceAtMost(Int.MAX_VALUE.toLong())
        .toInt()
    if (initialHeadBytes <= 0) return null

    var totalProbeBytes = initialHeadBytes.toLong()
    var headBytes = readBytes(0L, initialHeadBytes)
    val requiredHeadBytes = RemoteAudioMetadataProbe.requiredExpandedHeadBytes(relativePath, headBytes)
    if (requiredHeadBytes != null && requiredHeadBytes > headBytes.size) {
        if (requiredHeadBytes > RemoteAudioMetadataProbe.MAX_HEAD_PROBE_BYTES) {
            return null
        }
        val expandedHeadBytes = (normalizedSize ?: requiredHeadBytes)
            .coerceAtLeast(requiredHeadBytes)
            .coerceAtMost(RemoteAudioMetadataProbe.MAX_HEAD_PROBE_BYTES)
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
        totalProbeBytes = expandedHeadBytes.toLong()
        headBytes = readBytes(0L, expandedHeadBytes)
    }

    val tailBytes = if (normalizedSize != null && RemoteAudioMetadataProbe.shouldReadTail(relativePath)) {
        val requestedTailBytes = normalizedSize
            .coerceAtMost(RemoteAudioMetadataProbe.TAIL_PROBE_BYTES)
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
        if (totalProbeBytes + requestedTailBytes > RemoteAudioMetadataProbe.MAX_TOTAL_PROBE_BYTES) {
            null
        } else {
            readBytes((normalizedSize - requestedTailBytes.toLong()).coerceAtLeast(0L), requestedTailBytes)
        }
    } else {
        null
    }

    return RemoteAudioMetadataProbe.parse(
        relativePath = relativePath,
        headBytes = headBytes,
        tailBytes = tailBytes,
    )
}

internal fun AudioTagSnapshot.mergeEmbeddedLyrics(metadata: RemoteAudioMetadata?): AudioTagSnapshot {
    val lyrics = metadata?.embeddedLyrics
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: return this
    return copy(embeddedLyrics = lyrics)
}
