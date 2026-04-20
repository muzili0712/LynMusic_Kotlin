package top.iwesley.lyn.music.online

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import top.iwesley.lyn.music.online.resolve.KwUrlResolver
import top.iwesley.lyn.music.online.source.createPlatformCrypto
import top.iwesley.lyn.music.online.types.Quality
import top.iwesley.lyn.music.scripting.MusicSourceException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class KwUrlResolverTest {

    private val happyJson = """{"code":200,"url":"https://sycdn.kuwo.cn/abc.mp3"}"""

    private fun mockHttp(json: String, status: HttpStatusCode = HttpStatusCode.OK): HttpClient =
        HttpClient(MockEngine { _ ->
            respond(
                content = ByteReadChannel(json),
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        })

    private fun mockHttpCapturing(
        captured: MutableList<HttpRequestData>,
        json: String,
    ): HttpClient = HttpClient(MockEngine { req ->
        captured += req
        respond(
            content = ByteReadChannel(json),
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json"),
        )
    })

    @Test
    fun resolves_kw_url_for_k320() = runTest {
        val resolver = KwUrlResolver(
            http = mockHttp(happyJson),
            crypto = createPlatformCrypto(),
        )
        val url = resolver.resolve("12345", Quality.K320)
        assertTrue(
            url.url.startsWith("https://sycdn.kuwo.cn/"),
            "unexpected url: ${url.url}",
        )
        assertEquals(Quality.K320, url.quality)
    }

    /** url 缺失 / 空串 都映射到 UrlExpired。 */
    @Test
    fun throws_on_missing_url() = runTest {
        val missingUrl = """{"code":200}"""
        val resolverMissing = KwUrlResolver(
            http = mockHttp(missingUrl),
            crypto = createPlatformCrypto(),
        )
        assertFailsWith<MusicSourceException.UrlExpired> {
            resolverMissing.resolve("12345", Quality.K320)
        }

        val emptyUrl = """{"code":200,"url":""}"""
        val resolverEmpty = KwUrlResolver(
            http = mockHttp(emptyUrl),
            crypto = createPlatformCrypto(),
        )
        assertFailsWith<MusicSourceException.UrlExpired> {
            resolverEmpty.resolve("12345", Quality.K320)
        }
    }

    /**
     * 业务层 code != 200（例如 500 写在 body 里、HTTP 还是 200）→ UrlExpired。
     * 注意与 R7 的 HTTP 5xx → Network 区分。
     */
    @Test
    fun throws_on_error_code() = runTest {
        val errorBody = """{"code":500,"url":"https://sycdn.kuwo.cn/abc.mp3"}"""
        val resolver = KwUrlResolver(
            http = mockHttp(errorBody),
            crypto = createPlatformCrypto(),
        )
        assertFailsWith<MusicSourceException.UrlExpired> {
            resolver.resolve("12345", Quality.K320)
        }
    }

    /** R3: BR_MAP 不含 WAV → SourceDisabled。 */
    @Test
    fun unsupported_quality_throws_source_disabled() = runTest {
        val resolver = KwUrlResolver(
            http = mockHttp("""{}"""),
            crypto = createPlatformCrypto(),
        )
        assertFailsWith<MusicSourceException.SourceDisabled> {
            resolver.resolve("12345", Quality.WAV)
        }
    }

    /** R4: 抓请求，断言 host / path / method / 全部 query 形状。 */
    @Test
    fun outbound_request_matches_expected_shape() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val resolver = KwUrlResolver(
            http = mockHttpCapturing(captured, happyJson),
            crypto = createPlatformCrypto(),
        )
        resolver.resolve("12345", Quality.K320)
        val req = captured.single()
        assertEquals("ac.kuwo.cn", req.url.host)
        assertEquals("/url", req.url.encodedPath)
        assertEquals("GET", req.method.value)
        assertEquals("mp3", req.url.parameters["format"])
        assertEquals("12345", req.url.parameters["rid"])
        assertEquals("url", req.url.parameters["response"])
        assertEquals("convert_url3", req.url.parameters["type"])
        assertEquals("320kmp3", req.url.parameters["br"])
    }

    /** R7: HTTP 5xx → Network(code=500)，在解析前抛。 */
    @Test
    fun maps_5xx_to_network_exception() = runTest {
        val resolver = KwUrlResolver(
            http = mockHttp("", status = HttpStatusCode.InternalServerError),
            crypto = createPlatformCrypto(),
        )
        val ex = assertFailsWith<MusicSourceException.Network> {
            resolver.resolve("12345", Quality.K320)
        }
        assertEquals(500, ex.code)
    }
}
