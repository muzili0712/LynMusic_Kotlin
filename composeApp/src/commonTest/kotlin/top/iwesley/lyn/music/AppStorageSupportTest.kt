package top.iwesley.lyn.music

import kotlin.test.Test
import kotlin.test.assertEquals
import top.iwesley.lyn.music.platform.isAndroidPlaybackCacheFileName

class AppStorageSupportTest {
    @Test
    fun `android playback cache file name only matches known smb source ids`() {
        assertEquals(true, isAndroidPlaybackCacheFileName("smb-1-song.mp3", listOf("smb-1")))
        assertEquals(false, isAndroidPlaybackCacheFileName("smb-2-song.mp3", listOf("smb-1")))
        assertEquals(false, isAndroidPlaybackCacheFileName("artwork-cache", listOf("smb-1")))
        assertEquals(false, isAndroidPlaybackCacheFileName("smb-1-", listOf("smb-1")))
    }
}
