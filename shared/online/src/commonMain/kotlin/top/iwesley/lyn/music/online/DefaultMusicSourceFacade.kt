package top.iwesley.lyn.music.online

import top.iwesley.lyn.music.online.source.JsMusicSource
import top.iwesley.lyn.music.online.types.OnlineLyric
import top.iwesley.lyn.music.online.types.OnlineMusicId
import top.iwesley.lyn.music.online.types.OnlineSong
import top.iwesley.lyn.music.online.types.PlayableUrl
import top.iwesley.lyn.music.online.types.Quality
import top.iwesley.lyn.music.online.types.SearchPage
import top.iwesley.lyn.music.online.types.SourceInfo
import top.iwesley.lyn.music.online.types.SourceManifest
import top.iwesley.lyn.music.scripting.JsBridge
import top.iwesley.lyn.music.scripting.MusicSourceException

/**
 * [MusicSourceFacade] 的默认聚合实现。持有五个启用源（kw/kg/tx/wy/mg）各自的 [JsMusicSource]；
 * 对禁用源（如 xm）调用会直接抛 [MusicSourceException.SourceDisabled]。
 *
 * 使用方式：
 * ```
 *  val facade = DefaultMusicSourceFacade.build { sourceId ->
 *      JsBridgeImpl(http, platformTag = "jvm", userAgent = "...", crypto = createPlatformCrypto())
 *  }
 * ```
 *
 * 注：[sources] 暴露全部 6 条（含 xm），UI 层按 `enabled` 过滤；此设计便于展示"暂不可用"状态。
 */
class DefaultMusicSourceFacade(
    private val sourcesById: Map<String, JsMusicSource>,
) : MusicSourceFacade {

    override val sources: List<SourceInfo> = SourceManifest.all

    private fun require(sourceId: String): JsMusicSource =
        sourcesById[sourceId]
            ?: throw MusicSourceException.SourceDisabled(
                sourceId,
                "source not enabled in this build",
            )

    override suspend fun search(
        sourceId: String,
        keyword: String,
        page: Int,
        limit: Int,
    ): SearchPage<OnlineSong> = require(sourceId).search(keyword, page, limit)

    override suspend fun getPlayableUrl(
        id: OnlineMusicId,
        quality: Quality,
    ): PlayableUrl = require(id.source).getPlayableUrl(id.songmid, quality)

    override suspend fun getLyric(id: OnlineMusicId): OnlineLyric =
        require(id.source).getLyric(id.songmid)

    override suspend fun getPic(id: OnlineMusicId): String =
        require(id.source).getPic(id.songmid)

    companion object {
        /**
         * 把 [SourceManifest.enabled] 中的五个启用源逐个实例化 [JsMusicSource]；
         * 每个源用 [bridgeFactory] 生成独立的 [JsBridge]（大部分字段可共享，如 http/crypto，
         * 但 platformTag/userAgent 可按源定制 UA）。
         */
        fun build(bridgeFactory: (sourceId: String) -> JsBridge): DefaultMusicSourceFacade {
            val map = SourceManifest.enabled.associate { info ->
                info.id to JsMusicSource(info, bridgeFactory(info.id))
            }
            return DefaultMusicSourceFacade(map)
        }
    }
}
