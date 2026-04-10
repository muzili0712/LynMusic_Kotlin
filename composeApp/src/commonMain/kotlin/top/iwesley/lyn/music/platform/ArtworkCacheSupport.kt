package top.iwesley.lyn.music.platform

import top.iwesley.lyn.music.core.model.inferArtworkFileExtension
import top.iwesley.lyn.music.core.model.resolveArtworkCacheTarget as resolveSharedArtworkCacheTarget
import top.iwesley.lyn.music.core.model.stableArtworkCacheHash as stableSharedArtworkCacheHash

internal suspend fun resolveArtworkCacheTarget(locator: String?): String? {
    return resolveSharedArtworkCacheTarget(locator)
}

internal fun artworkCacheExtension(
    locator: String,
    bytes: ByteArray? = null,
): String {
    return inferArtworkFileExtension(locator = locator, bytes = bytes)
}

internal fun String.stableArtworkCacheHash(): String {
    return with(this) { stableSharedArtworkCacheHash() }
}
