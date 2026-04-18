package top.iwesley.lyn.music.platform

import top.iwesley.lyn.music.core.model.NonNavidromeAudioScanResult
import top.iwesley.lyn.music.core.model.classifyNonNavidromeAudioFile

private val JVM_SUPPORTED_NON_NAVIDROME_IMPORT_AUDIO_EXTENSIONS = setOf(
    "mp3",
    "m4a",
    "aac",
    "wav",
    "flac",
    "ape",
)

internal fun classifyJvmScannedAudioFile(fileName: String): NonNavidromeAudioScanResult {
    return classifyNonNavidromeAudioFile(
        fileName = fileName,
        supportedImportExtensions = JVM_SUPPORTED_NON_NAVIDROME_IMPORT_AUDIO_EXTENSIONS,
    )
}
