package top.iwesley.lyn.music.online.types

import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * 可播放的直链 + 其获取时刻。
 *
 * M0 采用保守过期策略：5 分钟（[DEFAULT_TTL_SECONDS]）内视为有效，之后调用方应重新请求。
 * 不同源实际有效期差异较大（腾讯短至 5 分钟，酷我可达数小时），此处取下界确保稳定回放。
 */
data class PlayableUrl(
    val url: String,
    val quality: Quality,
    val fetchedAt: Instant,
) {
    /**
     * 相对 [now] 判断是否过期；默认 [DEFAULT_TTL_SECONDS] 秒。
     */
    fun isExpired(
        now: Instant = Clock.System.now(),
        ttlSeconds: Int = DEFAULT_TTL_SECONDS,
    ): Boolean = (now - fetchedAt) >= ttlSeconds.seconds

    companion object {
        /** 保守 URL 过期阈值（秒）。M0 统一取 5 分钟。 */
        const val DEFAULT_TTL_SECONDS: Int = 300
    }
}
