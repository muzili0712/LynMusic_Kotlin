package top.iwesley.lyn.music

import java.awt.GraphicsEnvironment
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import top.iwesley.lyn.music.core.model.DEFAULT_LYRICS_SHARE_FONT_FAMILY
import top.iwesley.lyn.music.core.model.LyricsShareCardModel
import top.iwesley.lyn.music.core.model.LyricsShareTemplate
import top.iwesley.lyn.music.platform.JvmLyricsSharePlatformService
import top.iwesley.lyn.music.platform.isJvmLyricsShareAlphabeticFamilyName
import top.iwesley.lyn.music.platform.lyricsShareFontWhitelistForDesktop
import top.iwesley.lyn.music.platform.prioritizeJvmLyricsShareFontFamilyNames

class JvmLyricsShareFontSupportTest {

    private val service = JvmLyricsSharePlatformService()

    @Test
    fun `listAvailableFontFamilies returns all system fonts with prioritized prefix`() = runBlocking {
        val fonts = service.listAvailableFontFamilies().getOrThrow()
        val availableSystemFonts = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .availableFontFamilyNames
            .mapNotNull { familyName -> familyName.trim().takeIf { it.isNotEmpty() } }
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }
        val whitelist = lyricsShareFontWhitelistForDesktop(System.getProperty("os.name").orEmpty())
        val prioritizedPrefix = whitelist.mapNotNull { preset ->
            availableSystemFonts.firstOrNull { familyName ->
                familyName.equals(preset.familyName, ignoreCase = true)
            }?.let { familyName ->
                familyName to preset.previewText
            }
        }
        val expectedOrder = prioritizedPrefix.map { it.first } +
            availableSystemFonts
                .filterNot { familyName ->
                    prioritizedPrefix.any { prioritized ->
                        prioritized.first.equals(familyName, ignoreCase = true)
                    }
                }
                .partition(::isJvmLyricsShareAlphabeticFamilyName)
                .let { (alphabeticFonts, nonAlphabeticFonts) -> alphabeticFonts + nonAlphabeticFonts }

        assertTrue(fonts.isNotEmpty())
        assertEquals(fonts.map { it.familyName.lowercase() }.distinct(), fonts.map { it.familyName.lowercase() })
        assertContentEquals(
            expectedOrder.map { it.lowercase() },
            fonts.map { it.familyName.lowercase() },
        )
        assertContentEquals(
            prioritizedPrefix.map { it.first },
            fonts.take(prioritizedPrefix.size).map { it.familyName },
        )
        assertContentEquals(
            prioritizedPrefix.map { it.second },
            fonts.take(prioritizedPrefix.size).map { it.previewText },
        )
        assertTrue(fonts.take(prioritizedPrefix.size).all { it.isPrioritized })
        assertTrue(fonts.drop(prioritizedPrefix.size).all { !it.isPrioritized && it.previewText == it.familyName })
    }

    @Test
    fun `prioritizeJvmLyricsShareFontFamilyNames keeps macos whitelist at top and appends others`() {
        val prioritized = prioritizeJvmLyricsShareFontFamilyNames(
            osName = "macOS 15.0",
            availableFonts = listOf("Times New Roman", "Baskerville", "PingFang SC", "Avenir Next", "Arial"),
        )

        assertEquals(
            listOf("PingFang SC", "Avenir Next", "Baskerville", "Times New Roman", "Arial"),
            prioritized.map { it.familyName },
        )
        assertEquals(
            listOf("你好", "Hello", "Hello", "Hello", "Arial"),
            prioritized.map { it.previewText },
        )
        assertContentEquals(
            listOf(true, true, true, true, false),
            prioritized.map { it.isPrioritized },
        )
    }

    @Test
    fun `prioritizeJvmLyricsShareFontFamilyNames keeps windows whitelist at top and appends others`() {
        val prioritized = prioritizeJvmLyricsShareFontFamilyNames(
            osName = "Windows 11",
            availableFonts = listOf("Georgia", "Segoe UI", "SimSun", "Arial", "Microsoft YaHei"),
        )

        assertEquals(
            listOf("SimSun", "Microsoft YaHei", "Segoe UI", "Georgia", "Arial"),
            prioritized.map { it.familyName },
        )
        assertEquals(
            listOf("你好", "你好", "Hello", "Hello", "Arial"),
            prioritized.map { it.previewText },
        )
    }

    @Test
    fun `prioritizeJvmLyricsShareFontFamilyNames keeps linux whitelist at top and appends others`() {
        val prioritized = prioritizeJvmLyricsShareFontFamilyNames(
            osName = "Linux",
            availableFonts = listOf("DejaVu Serif", "Noto Serif", "Noto Sans", "Arial", "Noto Sans CJK SC"),
        )

        assertEquals(
            listOf("Noto Sans CJK SC", "Noto Sans", "Noto Serif", "DejaVu Serif", "Arial"),
            prioritized.map { it.familyName },
        )
        assertEquals(
            listOf("你好", "Hello", "Hello", "Hello", "Arial"),
            prioritized.map { it.previewText },
        )
    }

    @Test
    fun `prioritizeJvmLyricsShareFontFamilyNames returns all fonts when whitelist has no matches`() {
        val prioritized = prioritizeJvmLyricsShareFontFamilyNames(
            osName = "Linux",
            availableFonts = listOf("Arial", "Courier New"),
        )

        assertEquals(listOf("Arial", "Courier New"), prioritized.map { it.familyName })
        assertTrue(prioritized.all { !it.isPrioritized })
        assertContentEquals(listOf("Arial", "Courier New"), prioritized.map { it.previewText })
    }

    @Test
    fun `prioritizeJvmLyricsShareFontFamilyNames moves non alphabetic families to end`() {
        val prioritized = prioritizeJvmLyricsShareFontFamilyNames(
            osName = "Linux",
            availableFonts = listOf(".Apple Symbols", "Arial", "你好字体", "Courier New"),
        )

        assertEquals(
            listOf("Arial", "Courier New", ".Apple Symbols", "你好字体"),
            prioritized.map { it.familyName },
        )
    }

    @Test
    fun `buildPreview succeeds with a valid system font family`() = runBlocking {
        val fontFamily = service.listAvailableFontFamilies().getOrThrow().first().familyName

        val preview = renderPreview(fontFamilyName = fontFamily)

        assertTrue(preview.isNotEmpty())
    }

    @Test
    fun `buildPreview falls back when font family is invalid`() {
        val expected = renderPreview(fontFamilyName = DEFAULT_LYRICS_SHARE_FONT_FAMILY)
        val actual = renderPreview(fontFamilyName = "Definitely Missing Font Family")

        assertContentEquals(expected, actual)
    }

    private fun renderPreview(
        fontFamilyName: String?,
    ): ByteArray {
        return runBlocking {
            service.buildPreview(
                LyricsShareCardModel(
                    title = "字体测试",
                    artistName = "LynMusic",
                    artworkLocator = null,
                    template = LyricsShareTemplate.NOTE,
                    lyricsLines = listOf("第一句", "第二句"),
                    fontFamilyName = fontFamilyName,
                ),
            ).getOrThrow()
        }
    }
}
