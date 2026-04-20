package top.iwesley.lyn.music.online.resolve

import top.iwesley.lyn.music.core.model.DiagnosticLogLevel
import top.iwesley.lyn.music.core.model.GlobalDiagnosticLogger
import top.iwesley.lyn.music.online.diagnostics.OnlineLogTags
import top.iwesley.lyn.music.online.repository.OnlineMusicRepository
import top.iwesley.lyn.music.online.types.OnlineMusicId
import top.iwesley.lyn.music.online.types.OnlineSong
import top.iwesley.lyn.music.online.types.Quality
import top.iwesley.lyn.music.scripting.MusicSourceException

/**
 * 解析结果：真正可播放的直链 + 最终使用的音质 + 实际命中的源 id。
 *
 * `sourceId` 在跨源兜底（findMusic）命中时可能与原始 [OnlineMusicId.source] 不同，
 * 调用方如需展示 "通过 xx 源播放" 可以直接读取该字段。
 */
data class ResolvedUrl(
    val url: String,
    val quality: Quality,
    val sourceId: String,
)

/**
 * 在线歌曲 URL 解析器：同源音质降级 → 跨源 findMusic 兜底。
 *
 * - 同源：按 [Quality.degradationOrder] 从 `preferredQuality` 向下依次重试。
 * - 跨源：同源全部失败且 `songContext` 非空时，调用 [FindMusicM0] 在其它启用源里搜替代歌曲。
 * - 全失败：抛 [MusicSourceException.UrlExpired]（用原始源 id）。
 *
 * M0 不做 URL 缓存；直链 TTL 太短（见 [top.iwesley.lyn.music.online.types.PlayableUrl.DEFAULT_TTL_SECONDS]），
 * 缓存反而放大"用到过期链接"的风险。M1 会引入 `fetchedAt` 感知的短期缓存。
 */
interface SongUrlResolver {
    /**
     * 输入在线歌曲 ID + 偏好音质；返回可播放的 [ResolvedUrl]；全部失败抛 [MusicSourceException]。
     *
     * @param songContext 提供则启用跨源 findMusic 兜底；不提供则仅做同源降级。
     */
    suspend fun resolve(
        id: OnlineMusicId,
        preferredQuality: Quality,
        songContext: OnlineSong? = null,
    ): ResolvedUrl
}

/**
 * 默认实现。依赖 [OnlineMusicRepository] 拉 URL、[FindMusicM0] 做跨源匹配。
 *
 * [sourceResolvers]（M1.0 引入）：Kotlin-first 单源 URL 拿取器；若源 id 命中则优先使用，
 * 否则退回 [repository] 的 JS 引擎路径。Task 7 建立路由骨架，Tasks 8-12 按源接入各 Resolver。
 */
class DefaultSongUrlResolver(
    private val repository: OnlineMusicRepository,
    private val findMusic: FindMusicM0,
    private val sourceResolvers: Map<String, SourceUrlResolver> = emptyMap(),
) : SongUrlResolver {

    override suspend fun resolve(
        id: OnlineMusicId,
        preferredQuality: Quality,
        songContext: OnlineSong?,
    ): ResolvedUrl {
        // 从偏好音质开始降级；若 preferredQuality 不在序列里（理论上不会发生），退回完整序列保底。
        val degrade = Quality.degradationOrder
            .dropWhile { it != preferredQuality }
            .ifEmpty { Quality.degradationOrder }

        // 同源降级；优先 Kotlin sourceResolver，缺席时退回 JS repository。
        for (q in degrade) {
            val kotlinResolver = sourceResolvers[id.source]
            try {
                val playable = if (kotlinResolver != null) {
                    kotlinResolver.resolve(id.songmid, q)
                } else {
                    repository.getPlayableUrl(id, q)
                }
                log(id.source, "url-ok q=${q.lxKey} via=${if (kotlinResolver != null) "kotlin" else "js"}")
                return ResolvedUrl(playable.url, q, id.source)
            } catch (e: MusicSourceException) {
                log(
                    sourceId = id.source,
                    message = "url-fail q=${q.lxKey}: ${e.message}",
                    level = DiagnosticLogLevel.WARN,
                    t = e,
                )
                continue
            }
        }

        // 跨源 findMusic（M0 仅在有 songContext 时执行）；替代源同样优先 Kotlin Resolver。
        if (songContext != null) {
            val alt = findMusic.find(songContext, excludeSource = id.source)
            if (alt != null) {
                try {
                    val altResolver = sourceResolvers[alt.id.source]
                    val playable = if (altResolver != null) {
                        altResolver.resolve(alt.id.songmid, alt.defaultQuality)
                    } else {
                        repository.getPlayableUrl(alt.id, alt.defaultQuality)
                    }
                    log(
                        sourceId = id.source,
                        message = "cross-source-fallback source=${alt.id.source} q=${alt.defaultQuality.lxKey} via=${if (altResolver != null) "kotlin" else "js"}",
                    )
                    return ResolvedUrl(playable.url, alt.defaultQuality, alt.id.source)
                } catch (e: MusicSourceException) {
                    log(
                        sourceId = alt.id.source,
                        message = "cross-fail: ${e.message}",
                        level = DiagnosticLogLevel.ERROR,
                        t = e,
                    )
                }
            }
        }

        throw MusicSourceException.UrlExpired(id.source)
    }

    private fun log(
        sourceId: String,
        message: String,
        level: DiagnosticLogLevel = DiagnosticLogLevel.DEBUG,
        t: Throwable? = null,
    ) {
        GlobalDiagnosticLogger.log(level, OnlineLogTags.source(sourceId), message, t)
    }
}
