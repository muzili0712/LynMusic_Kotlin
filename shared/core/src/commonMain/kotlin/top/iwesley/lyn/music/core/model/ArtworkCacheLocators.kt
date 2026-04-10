package top.iwesley.lyn.music.core.model

fun normalizedArtworkCacheLocator(locator: String?): String? {
    val rawTarget = normalizeArtworkLocator(locator)?.trim().orEmpty()
    return rawTarget.takeIf { it.isNotBlank() }
}

suspend fun resolveArtworkCacheTarget(locator: String?): String? {
    val rawTarget = normalizedArtworkCacheLocator(locator) ?: return null
    val target = if (parseNavidromeCoverLocator(rawTarget) != null) {
        NavidromeLocatorRuntime.resolveCoverArtUrl(rawTarget).orEmpty()
    } else {
        rawTarget
    }
    return target.takeIf { it.isNotBlank() }
}

fun String.stableArtworkCacheHash(): String {
    var hash = 14695981039346656037uL
    encodeToByteArray().forEach { byte ->
        hash = (hash xor byte.toUByte().toULong()) * 1099511628211uL
    }
    return hash.toString(16).padStart(16, '0')
}
