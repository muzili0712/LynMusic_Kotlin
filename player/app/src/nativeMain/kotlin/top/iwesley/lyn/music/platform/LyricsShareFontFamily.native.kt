package top.iwesley.lyn.music.platform

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.SystemFont
import top.iwesley.lyn.music.core.model.parseLyricsShareImportedFontHash

@OptIn(ExperimentalTextApi::class)
actual fun lyricsSharePreviewFontFamily(
    fontKey: String?,
    displayName: String?,
    fontFilePath: String?,
): FontFamily? {
    if (fontKey?.let(::parseLyricsShareImportedFontHash) != null) return null
    val normalizedFamilyName = (displayName ?: fontKey)?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return runCatching {
        FontFamily(SystemFont(normalizedFamilyName))
    }.getOrNull()
}
