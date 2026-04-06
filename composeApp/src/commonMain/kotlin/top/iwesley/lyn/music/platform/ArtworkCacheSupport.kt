package top.iwesley.lyn.music.platform

import top.iwesley.lyn.music.core.model.NavidromeLocatorRuntime
import top.iwesley.lyn.music.core.model.normalizeArtworkLocator
import top.iwesley.lyn.music.core.model.parseNavidromeCoverLocator

private val KNOWN_ARTWORK_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "bmp", "gif")

internal suspend fun resolveArtworkCacheTarget(locator: String?): String? {
    val rawTarget = normalizeArtworkLocator(locator)?.trim().orEmpty()
    if (rawTarget.isBlank()) return null
    val target = if (parseNavidromeCoverLocator(rawTarget) != null) {
        NavidromeLocatorRuntime.resolveCoverArtUrl(rawTarget).orEmpty()
    } else {
        rawTarget
    }
    return target.takeIf { it.isNotBlank() }
}

internal fun artworkCacheExtension(locator: String): String {
    val path = locator
        .substringBefore('#')
        .substringBefore('?')
        .substringAfterLast('/', "")
    val extension = path.substringAfterLast('.', "").lowercase()
    return if (extension in KNOWN_ARTWORK_EXTENSIONS) ".$extension" else ".img"
}

internal fun String.stableArtworkCacheHash(): String {
    var hash = 2166136261u
    encodeToByteArray().forEach { byte ->
        hash = (hash xor byte.toUByte().toUInt()) * 16777619u
    }
    return hash.toString(16)
}
