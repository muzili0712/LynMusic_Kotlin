package top.iwesley.lyn.music.online

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import top.iwesley.lyn.music.core.model.Track
import top.iwesley.lyn.music.online.adapter.asOnlineMusicIdOrNull
import top.iwesley.lyn.music.online.adapter.toTrack
import top.iwesley.lyn.music.online.types.OnlineMusicId
import top.iwesley.lyn.music.online.types.OnlineSong
import top.iwesley.lyn.music.online.types.Quality

class OnlineSongToTrackTest {

    @Test
    fun toTrack_encodes_lazy_locator_with_quality_key() {
        val song = sampleOnlineSong(preferredQuality = Quality.K320)

        val track = song.toTrack(preferredQuality = Quality.FLAC.lxKey)

        assertEquals("online-kw-KW-123", track.id)
        assertEquals("online-kw", track.sourceId)
        assertEquals("online-lazy://kw/KW-123?q=flac", track.mediaLocator)
        assertEquals("Song Name", track.title)
        assertEquals("Artist A", track.artistName)
        assertEquals("Album X", track.albumTitle)
        assertEquals(210_000L, track.durationMs)
        assertEquals("https://cover.example/xx.jpg", track.artworkLocator)
        assertEquals("", track.relativePath)
    }

    @Test
    fun round_trip_through_asOnlineMusicIdOrNull() {
        val song = sampleOnlineSong(preferredQuality = Quality.K320)

        val track = song.toTrack()
        val id = track.asOnlineMusicIdOrNull()

        assertEquals(OnlineMusicId(source = "kw", songmid = "KW-123"), id)
    }

    @Test
    fun local_track_returns_null() {
        val localTrack = Track(
            id = "local-track-1",
            sourceId = "local-1",
            title = "Local Song",
            artistName = "Local Artist",
            albumTitle = "Local Album",
            durationMs = 180_000L,
            mediaLocator = "file:///music/local.mp3",
            relativePath = "Local Artist/Local Song.mp3",
        )

        assertNull(localTrack.asOnlineMusicIdOrNull())
    }

    private fun sampleOnlineSong(preferredQuality: Quality): OnlineSong {
        return OnlineSong(
            id = OnlineMusicId(source = "kw", songmid = "KW-123"),
            name = "Song Name",
            singer = "Artist A",
            album = "Album X",
            albumId = null,
            intervalSeconds = 210,
            coverUrl = "https://cover.example/xx.jpg",
            availableQualities = listOf(Quality.K128, Quality.K320, Quality.FLAC),
            defaultQuality = preferredQuality,
        )
    }
}
