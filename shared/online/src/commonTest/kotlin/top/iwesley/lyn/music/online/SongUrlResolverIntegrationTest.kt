package top.iwesley.lyn.music.online

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import top.iwesley.lyn.music.online.repository.OnlineMusicRepository
import top.iwesley.lyn.music.online.resolve.DefaultSongUrlResolver
import top.iwesley.lyn.music.online.resolve.FindMusicM0
import top.iwesley.lyn.music.online.resolve.KwUrlResolver
import top.iwesley.lyn.music.online.source.createPlatformCrypto
import top.iwesley.lyn.music.online.types.OnlineMusicId
import top.iwesley.lyn.music.online.types.Quality
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * M1.0 T13 集成烟雾：验证 Task 7 的 Kotlin-first 路由（DefaultSongUrlResolver.sourceResolvers）
 * 与 Task 10 的 KwUrlResolver 组合时能端到端工作，**不经过** JS 门面 [MusicSourceFacade.getPlayableUrl]。
 *
 * 对比 [SongUrlResolverTest]：后者验证 DefaultSongUrlResolver 在**空 sourceResolvers** 下的路由
 * （全部 fallthrough 到 facade），这里反过来验证"当 kw 映射到 Kotlin resolver 时优先走它"。
 */
class SongUrlResolverIntegrationTest {

    @Test
    fun kw_resolver_wins_over_js_facade_when_mapped() = runTest {
        val mockHttp = HttpClient(MockEngine {
            respond(
                content = ByteReadChannel("""{"code":200,"url":"https://sycdn.kuwo.cn/abc.mp3"}"""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        })
        // 如果代码误走 JS 门面，FakeMusicSourceFacade 会返回 "https://fake/..." 开头的 URL
        // —— 与真实 kw 链形成对照，断言得以区分。
        val facade = FakeMusicSourceFacade()
        val repo = OnlineMusicRepository(facade)
        val crypto = createPlatformCrypto()
        val kwResolver = KwUrlResolver(mockHttp, crypto)

        val songResolver = DefaultSongUrlResolver(
            repository = repo,
            findMusic = FindMusicM0(repo),
            sourceResolvers = mapOf("kw" to kwResolver),
        )

        val resolved = songResolver.resolve(OnlineMusicId("kw", "songmid-123"), Quality.K320)

        assertEquals("https://sycdn.kuwo.cn/abc.mp3", resolved.url)
        assertEquals(Quality.K320, resolved.quality)
        assertEquals("kw", resolved.sourceId)
        assertEquals(
            0,
            facade.urlCallCount,
            "JS facade.getPlayableUrl should NOT be invoked when a Kotlin resolver is mapped for the source",
        )
        assertTrue(
            resolved.url.startsWith("https://sycdn.kuwo.cn/"),
            "Kotlin-first path must return real kw url, got: ${resolved.url}",
        )
    }
}
