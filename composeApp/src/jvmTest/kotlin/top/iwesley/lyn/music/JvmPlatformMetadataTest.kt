package top.iwesley.lyn.music

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import top.iwesley.lyn.music.platform.formatJvmVlcParseStatus
import top.iwesley.lyn.music.platform.resolveJvmVlcMetadataFallback
import top.iwesley.lyn.music.platform.sanitizeJvmVlcMetadataTitle

class JvmPlatformMetadataTest {

    @Test
    fun `formats missing vlc parse status`() {
        assertEquals("UNKNOWN", formatJvmVlcParseStatus(null))
    }

    @Test
    fun `filters vlc internal callback titles`() {
        assertNull(sanitizeJvmVlcMetadataTitle("imem://"))
        assertNull(sanitizeJvmVlcMetadataTitle("IMEM://track-1"))
        assertNull(sanitizeJvmVlcMetadataTitle("fd://12"))
    }

    @Test
    fun `keeps real metadata titles`() {
        assertEquals("黄耀明 - 四季歌", sanitizeJvmVlcMetadataTitle("黄耀明 - 四季歌"))
        assertEquals("Song Title", sanitizeJvmVlcMetadataTitle("  Song Title  "))
    }

    @Test
    fun `prefers database metadata over vlc metadata`() {
        assertNull(
            resolveJvmVlcMetadataFallback(
                primaryValue = "数据库标题",
                vlcValue = "VLC 标题",
                previousValue = null,
            ),
        )
    }

    @Test
    fun `uses vlc metadata when database metadata is missing`() {
        assertEquals(
            "VLC 标题",
            resolveJvmVlcMetadataFallback(
                primaryValue = " ",
                vlcValue = " VLC 标题 ",
                previousValue = null,
            ),
        )
    }
}
