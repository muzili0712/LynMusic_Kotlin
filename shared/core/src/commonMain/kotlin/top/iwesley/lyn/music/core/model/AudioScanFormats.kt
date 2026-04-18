package top.iwesley.lyn.music.core.model

private val NON_NAVIDROME_SCANNABLE_AUDIO_EXTENSIONS = setOf(
    "mp3",
    "m4a",
    "aac",
    "wav",
    "flac",
    "ape",
    "ogg",
    "opus",
    "wma",
    "aiff",
    "aif",
)

const val UNSUPPORTED_AUDIO_IMPORT_REASON = "当前平台暂不支持导入该音频格式。"

enum class NonNavidromeAudioScanResult {
    NOT_AUDIO,
    IMPORT_UNSUPPORTED,
    IMPORT_SUPPORTED,
}

fun isNonNavidromeScannableAudioFile(fileName: String): Boolean {
    return audioFileExtension(fileName) in NON_NAVIDROME_SCANNABLE_AUDIO_EXTENSIONS
}

fun classifyNonNavidromeAudioFile(
    fileName: String,
    supportedImportExtensions: Set<String>,
): NonNavidromeAudioScanResult {
    val extension = audioFileExtension(fileName) ?: return NonNavidromeAudioScanResult.NOT_AUDIO
    if (extension !in NON_NAVIDROME_SCANNABLE_AUDIO_EXTENSIONS) {
        return NonNavidromeAudioScanResult.NOT_AUDIO
    }
    return if (extension in supportedImportExtensions) {
        NonNavidromeAudioScanResult.IMPORT_SUPPORTED
    } else {
        NonNavidromeAudioScanResult.IMPORT_UNSUPPORTED
    }
}

fun unsupportedAudioImportFailure(relativePath: String): ImportScanFailure {
    return ImportScanFailure(
        relativePath = relativePath,
        reason = UNSUPPORTED_AUDIO_IMPORT_REASON,
    )
}

private fun audioFileExtension(fileName: String): String? {
    val extension = fileName.substringAfterLast('.', "").trim().lowercase()
    return extension.takeIf { it.isNotBlank() }
}
