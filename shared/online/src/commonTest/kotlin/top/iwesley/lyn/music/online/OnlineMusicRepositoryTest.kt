package top.iwesley.lyn.music.online

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import top.iwesley.lyn.music.online.repository.OnlineMusicRepository
import top.iwesley.lyn.music.online.types.OnlineMusicId
import top.iwesley.lyn.music.online.types.Quality

/**
 * [OnlineMusicRepository] 契约测试：验证 M0 缓存策略 —— search / lyric 命中缓存；URL 永不缓存。
 */
class OnlineMusicRepositoryTest {

    @Test
    fun search_is_cached_by_source_keyword_and_page() = runTest {
        val fake = FakeMusicSourceFacade()
        val repo = OnlineMusicRepository(fake)

        val first = repo.search("kw", "jay", page = 1, limit = 30)
        val second = repo.search("kw", "jay", page = 1, limit = 30)

        assertEquals(first, second)
        assertEquals(1, fake.searchCallCount)

        // 不同页应 miss 缓存
        repo.search("kw", "jay", page = 2, limit = 30)
        assertEquals(2, fake.searchCallCount)

        // 不同源也应 miss 缓存
        repo.search("kg", "jay", page = 1, limit = 30)
        assertEquals(3, fake.searchCallCount)
    }

    @Test
    fun url_is_never_cached_even_for_same_id_and_quality() = runTest {
        val fake = FakeMusicSourceFacade()
        val repo = OnlineMusicRepository(fake)
        val id = OnlineMusicId("kw", "abc123")

        val a = repo.getPlayableUrl(id, Quality.K320)
        val b = repo.getPlayableUrl(id, Quality.K320)

        // 核心断言：facade 每次都被调用（没有缓存层）
        assertEquals(2, fake.urlCallCount)
        assertEquals(a.url, b.url)
        assertEquals(a.quality, b.quality)
    }

    @Test
    fun lyric_is_cached_by_stable_key() = runTest {
        val fake = FakeMusicSourceFacade()
        val repo = OnlineMusicRepository(fake)
        val id = OnlineMusicId("wy", "song-42")

        val first = repo.getLyric(id)
        val second = repo.getLyric(id)

        assertEquals(first, second)
        assertEquals(1, fake.lyricCallCount)

        // 不同 id 应 miss
        repo.getLyric(OnlineMusicId("wy", "song-43"))
        assertEquals(2, fake.lyricCallCount)
    }
}
