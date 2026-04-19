package top.iwesley.lyn.music.online

import kotlin.time.Clock
import top.iwesley.lyn.music.online.types.OnlineLyric
import top.iwesley.lyn.music.online.types.OnlineMusicId
import top.iwesley.lyn.music.online.types.OnlineSong
import top.iwesley.lyn.music.online.types.PlayableUrl
import top.iwesley.lyn.music.online.types.Quality
import top.iwesley.lyn.music.online.types.SearchPage
import top.iwesley.lyn.music.online.types.SourceInfo
import top.iwesley.lyn.music.online.types.SourceManifest

/**
 * Repository 层测试用假门面。用计数器 ([searchCallCount] 等) 验证缓存命中 —— 第一次调用走 facade
 * 计数 +1；缓存命中则不再触达 facade，计数保持不变。
 *
 * 返回内容足够测试辨识即可（stable id、页号回显等），不追求数据真实性。
 *
 * 可选构造参数：
 * - [searchResults]：按 `(sourceId to keyword)` 覆盖搜索返回；未命中时退回默认占位 song。
 * - [urlProvider]：覆盖 `getPlayableUrl` 行为（用于模拟音质失败 / 错误注入）。
 * 两个参数均为默认值，保证旧测试（`FakeMusicSourceFacade()` 无参）继续 work。
 */
class FakeMusicSourceFacade(
    private val searchResults: Map<Pair<String, String>, SearchPage<OnlineSong>> = emptyMap(),
    private val urlProvider: (suspend (OnlineMusicId, Quality) -> PlayableUrl)? = null,
) : MusicSourceFacade {

    override val sources: List<SourceInfo> = SourceManifest.all

    var searchCallCount: Int = 0
        private set
    var urlCallCount: Int = 0
        private set
    var lyricCallCount: Int = 0
        private set
    var picCallCount: Int = 0
        private set

    override suspend fun search(
        sourceId: String,
        keyword: String,
        page: Int,
        limit: Int,
    ): SearchPage<OnlineSong> {
        searchCallCount++
        searchResults[sourceId to keyword]?.let { return it }
        return SearchPage(
            items = listOf(
                OnlineSong(
                    id = OnlineMusicId(sourceId, "song-$keyword-$page"),
                    name = "result:$keyword",
                    singer = "singer",
                    album = null,
                    albumId = null,
                    intervalSeconds = 0,
                    coverUrl = null,
                    availableQualities = emptyList(),
                    defaultQuality = Quality.K320,
                ),
            ),
            page = page,
            totalPages = 1,
            totalItems = 1,
            sourceId = sourceId,
        )
    }

    override suspend fun getPlayableUrl(id: OnlineMusicId, quality: Quality): PlayableUrl {
        urlCallCount++
        urlProvider?.let { return it(id, quality) }
        return PlayableUrl(
            url = "https://fake/${id.stableKey}/${quality.lxKey}",
            quality = quality,
            fetchedAt = Clock.System.now(),
        )
    }

    override suspend fun getLyric(id: OnlineMusicId): OnlineLyric {
        lyricCallCount++
        return OnlineLyric(original = "[00:01]${id.stableKey}")
    }

    override suspend fun getPic(id: OnlineMusicId): String {
        picCallCount++
        return "https://fake/pic/${id.stableKey}"
    }
}
