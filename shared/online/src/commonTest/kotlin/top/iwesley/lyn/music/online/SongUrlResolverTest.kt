package top.iwesley.lyn.music.online

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Clock
import kotlinx.coroutines.test.runTest
import top.iwesley.lyn.music.online.repository.OnlineMusicRepository
import top.iwesley.lyn.music.online.resolve.DefaultSongUrlResolver
import top.iwesley.lyn.music.online.resolve.FindMusicM0
import top.iwesley.lyn.music.online.types.OnlineMusicId
import top.iwesley.lyn.music.online.types.PlayableUrl
import top.iwesley.lyn.music.online.types.Quality
import top.iwesley.lyn.music.scripting.MusicSourceException

class SongUrlResolverTest {

    @Test
    fun returns_url_at_preferred_quality_when_source_ok() = runTest {
        val facade = FakeMusicSourceFacade()
        val repo = OnlineMusicRepository(facade)
        val resolver = DefaultSongUrlResolver(repo, FindMusicM0(repo))
        val r = resolver.resolve(OnlineMusicId("kw", "1"), Quality.K320)
        assertEquals(Quality.K320, r.quality)
        assertEquals("kw", r.sourceId)
    }

    @Test
    fun degrades_to_next_quality_on_failure() = runTest {
        val facade = FakeMusicSourceFacade(
            urlProvider = { _, q ->
                if (q == Quality.K320) throw MusicSourceException.Network("kw", 403)
                PlayableUrl("u", q, Clock.System.now())
            },
        )
        val repo = OnlineMusicRepository(facade)
        val resolver = DefaultSongUrlResolver(repo, FindMusicM0(repo))
        val r = resolver.resolve(OnlineMusicId("kw", "1"), Quality.K320)
        // 降级序中 K320 失败后下一个是 K192
        assertEquals(Quality.K192, r.quality)
    }

    @Test
    fun throws_url_expired_when_all_qualities_fail_and_no_song_context() = runTest {
        val facade = FakeMusicSourceFacade(
            urlProvider = { _, _ -> throw MusicSourceException.Network("kw", 500) },
        )
        val repo = OnlineMusicRepository(facade)
        val resolver = DefaultSongUrlResolver(repo, FindMusicM0(repo))
        assertFailsWith<MusicSourceException.UrlExpired> {
            resolver.resolve(OnlineMusicId("kw", "1"), Quality.K320)
        }
    }
}
