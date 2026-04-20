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
import top.iwesley.lyn.music.online.resolve.MgUrlResolver
import top.iwesley.lyn.music.online.source.createPlatformCrypto
import top.iwesley.lyn.music.online.types.Quality
import top.iwesley.lyn.music.scripting.MusicSourceException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MgUrlResolverTest {

    private val happyJson =
        """{"resource":[{"newRateFormats":[{"formatType":"HQ","url":"http://aod.migu.cn/abc.mp3"}]}]}"""

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
    fun resolves_mg_url_for_k320() = runTest {
        val resolver = MgUrlResolver(
            http = mockHttp(happyJson),
            crypto = createPlatformCrypto(),
        )
        val url = resolver.resolve("600902000002739441", Quality.K320)
        assertTrue(
            url.url.startsWith("http://aod.migu.cn/"),
            "unexpected url: ${url.url}",
        )
        assertEquals(Quality.K320, url.quality)
    }

    /**
     * 兼容旧响应：`newRateFormats` 缺失时回退到同级 `rateFormats`。
     */
    @Test
    fun falls_back_to_rateFormats_when_newRateFormats_missing() = runTest {
        val body =
            """{"resource":[{"rateFormats":[{"formatType":"HQ","url":"http://aod.migu.cn/old.mp3"}]}]}"""
        val resolver = MgUrlResolver(
            http = mockHttp(body),
            crypto = createPlatformCrypto(),
        )
        val url = resolver.resolve("600902000002739441", Quality.K320)
        assertEquals("http://aod.migu.cn/old.mp3", url.url)
    }

    /**
     * entry 里 `url` 缺失时用 `androidUrl` 兜底。
     */
    @Test
    fun falls_back_to_androidUrl_when_url_missing() = runTest {
        val body =
            """{"resource":[{"newRateFormats":[{"formatType":"HQ","androidUrl":"http://aod.migu.cn/and.mp3"}]}]}"""
        val resolver = MgUrlResolver(
            http = mockHttp(body),
            crypto = createPlatformCrypto(),
        )
        val url = resolver.resolve("600902000002739441", Quality.K320)
        assertEquals("http://aod.migu.cn/and.mp3", url.url)
    }

    /**
     * protocol-relative URL（`//` 开头）自动补 `http:`。
     */
    @Test
    fun prepends_http_scheme_to_protocol_relative_urls() = runTest {
        val body =
            """{"resource":[{"newRateFormats":[{"formatType":"HQ","url":"//aod.migu.cn/rel.mp3"}]}]}"""
        val resolver = MgUrlResolver(
            http = mockHttp(body),
            crypto = createPlatformCrypto(),
        )
        val url = resolver.resolve("600902000002739441", Quality.K320)
        assertEquals("http://aod.migu.cn/rel.mp3", url.url)
    }

    /**
     * 响应里没有匹配请求 formatType 的条目（例如只有 PQ，但我们要 HQ）→ UrlExpired。
     */
    @Test
    fun throws_on_missing_format_type() = runTest {
        val body =
            """{"resource":[{"newRateFormats":[{"formatType":"PQ","url":"http://aod.migu.cn/pq.mp3"}]}]}"""
        val resolver = MgUrlResolver(
            http = mockHttp(body),
            crypto = createPlatformCrypto(),
        )
        assertFailsWith<MusicSourceException.UrlExpired> {
            resolver.resolve("600902000002739441", Quality.K320)
        }
    }

    /** R3: mg 无 192k 码率层 → Quality.K192 应抛 SourceDisabled。 */
    @Test
    fun unsupported_quality_throws_source_disabled() = runTest {
        val resolver = MgUrlResolver(
            http = mockHttp("""{}"""),
            crypto = createPlatformCrypto(),
        )
        assertFailsWith<MusicSourceException.SourceDisabled> {
            resolver.resolve("600902000002739441", Quality.K192)
        }
    }

    /**
     * R4: 抓请求，断言 host / path / method / 全部 query + User-Agent header 形状。
     */
    @Test
    fun outbound_request_matches_expected_shape() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val resolver = MgUrlResolver(
            http = mockHttpCapturing(captured, happyJson),
            crypto = createPlatformCrypto(),
        )
        val songmid = "600902000002739441"
        resolver.resolve(songmid, Quality.K320)
        val req = captured.single()
        assertEquals("app.c.nf.migu.cn", req.url.host)
        assertEquals("/MIGUM2.0/v1.0/content/resourceinfo.do", req.url.encodedPath)
        assertEquals("GET", req.method.value)
        assertEquals(songmid, req.url.parameters["resourceId"])
        assertEquals("2", req.url.parameters["resourceType"])
        assertEquals("orpheus", req.url.parameters["by"])

        val ua = req.headers["User-Agent"]
        assertTrue(
            ua != null && ua.contains("MiguMusic/"),
            "expected MiguMusic UA, got: $ua",
        )
    }

    /** R7: HTTP 5xx → Network(code=500)，在解析前抛。 */
    @Test
    fun maps_5xx_to_network_exception() = runTest {
        val resolver = MgUrlResolver(
            http = mockHttp("", status = HttpStatusCode.InternalServerError),
            crypto = createPlatformCrypto(),
        )
        val ex = assertFailsWith<MusicSourceException.Network> {
            resolver.resolve("600902000002739441", Quality.K320)
        }
        assertEquals(500, ex.code)
    }
}
