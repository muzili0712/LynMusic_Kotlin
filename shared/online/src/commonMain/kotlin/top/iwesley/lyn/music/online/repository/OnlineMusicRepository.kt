package top.iwesley.lyn.music.online.repository

import top.iwesley.lyn.music.core.model.DiagnosticLogLevel
import top.iwesley.lyn.music.core.model.GlobalDiagnosticLogger
import top.iwesley.lyn.music.online.MusicSourceFacade
import top.iwesley.lyn.music.online.cache.OnlineMemoryCache
import top.iwesley.lyn.music.online.diagnostics.OnlineLogTags
import top.iwesley.lyn.music.online.types.OnlineLyric
import top.iwesley.lyn.music.online.types.OnlineMusicId
import top.iwesley.lyn.music.online.types.OnlineSong
import top.iwesley.lyn.music.online.types.PlayableUrl
import top.iwesley.lyn.music.online.types.Quality
import top.iwesley.lyn.music.online.types.SearchPage
import top.iwesley.lyn.music.online.types.SourceInfo

/**
 * M0 在线音乐仓库层：在 [MusicSourceFacade] 之上加 LRU 缓存。
 *
 * 缓存策略：
 * - **search**：key = `"sourceId|keyword|page|limit"`，结果稳定（不同页/不同关键字单独缓存）。
 * - **lyric**：key = [OnlineMusicId.stableKey]；歌词内容稳定，可长时间缓存。
 * - **pic**：key = [OnlineMusicId.stableKey]；封面 URL 在 M0 视为稳定（过期由 UI 层降级处理）。
 * - **URL**：**不缓存**。直链 TTL 仅 5 分钟（见 [PlayableUrl.DEFAULT_TTL_SECONDS]），
 *   加缓存反而放大过期风险；由上层播放器按需调用并依据 [PlayableUrl.isExpired] 刷新。
 *
 * 并发：M0 假设每种操作由同一协程链串行访问同一 cache；真并发场景由 M1 再处理。
 */
class OnlineMusicRepository(
    private val facade: MusicSourceFacade,
    private val searchCache: OnlineMemoryCache<SearchPage<OnlineSong>> = OnlineMemoryCache(64),
    private val lyricCache: OnlineMemoryCache<OnlineLyric> = OnlineMemoryCache(128),
    private val picCache: OnlineMemoryCache<String> = OnlineMemoryCache(128),
) {

    /** 透传 [MusicSourceFacade.sources]，UI 用来渲染源切换器。 */
    val sources: List<SourceInfo> get() = facade.sources

    /**
     * 搜索。命中缓存直接返回；否则走 facade 并写入缓存。
     */
    suspend fun search(
        sourceId: String,
        keyword: String,
        page: Int = 1,
        limit: Int = 30,
    ): SearchPage<OnlineSong> {
        val key = searchKey(sourceId, keyword, page, limit)
        searchCache.get(key)?.let {
            logCache("hit $key")
            return it
        }
        logCache("miss $key")
        val fresh = facade.search(sourceId, keyword, page, limit)
        searchCache.put(key, fresh)
        return fresh
    }

    /**
     * 获取可播放直链。**不缓存** —— TTL 太短，缓存反而制造误用。
     */
    suspend fun getPlayableUrl(id: OnlineMusicId, quality: Quality): PlayableUrl =
        facade.getPlayableUrl(id, quality)

    /** 歌词；命中缓存直接返回。 */
    suspend fun getLyric(id: OnlineMusicId): OnlineLyric {
        val key = id.stableKey
        lyricCache.get(key)?.let {
            logCache("hit lyric:$key")
            return it
        }
        logCache("miss lyric:$key")
        val fresh = facade.getLyric(id)
        lyricCache.put(key, fresh)
        return fresh
    }

    /** 封面 URL；命中缓存直接返回。 */
    suspend fun getPic(id: OnlineMusicId): String {
        val key = id.stableKey
        picCache.get(key)?.let {
            logCache("hit pic:$key")
            return it
        }
        logCache("miss pic:$key")
        val fresh = facade.getPic(id)
        picCache.put(key, fresh)
        return fresh
    }

    /** 清空全部缓存；UI 下拉刷新或手动清理时调用。 */
    fun clearCaches() {
        searchCache.clear()
        lyricCache.clear()
        picCache.clear()
    }

    private fun searchKey(sourceId: String, keyword: String, page: Int, limit: Int): String =
        "$sourceId|$keyword|$page|$limit"

    private fun logCache(message: String) {
        GlobalDiagnosticLogger.log(DiagnosticLogLevel.DEBUG, OnlineLogTags.CACHE, message, null)
    }
}
