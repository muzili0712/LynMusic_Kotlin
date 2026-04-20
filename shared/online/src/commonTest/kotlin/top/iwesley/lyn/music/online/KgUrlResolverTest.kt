package top.iwesley.lyn.music.online

import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import top.iwesley.lyn.music.online.resolve.KgUrlResolver
import top.iwesley.lyn.music.online.source.createPlatformCrypto
import top.iwesley.lyn.music.online.types.Quality
import top.iwesley.lyn.music.scripting.MusicSourceException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class KgUrlResolverTest {

    private val happyJson =
        """{"status":1,"url":["http://fs.open.kugou.com/abc.mp3"]}"""

    @Test
    fun resolves_kg_url_for_k320() = runTest {
        val resolver = KgUrlResolver(
            http = mockHttp(happyJson),
            crypto = createPlatformCrypto(),
        )
        val url = resolver.resolve("abc123def456", Quality.K320)
        assertTrue(
            url.url.startsWith("http://fs.open.kugou.com/"),
            "unexpected url: ${url.url}",
        )
        assertEquals(Quality.K320, url.quality)
    }

    /** 空 url 数组 → UrlExpired。 */
    @Test
    fun throws_on_empty_url_array() = runTest {
        val resolver = KgUrlResolver(
            http = mockHttp("""{"status":1,"url":[]}"""),
            crypto = createPlatformCrypto(),
        )
        assertFailsWith<MusicSourceException.UrlExpired> {
            resolver.resolve("abc123def456", Quality.K320)
        }
    }

    /**
     * status != 1（例如酷狗内部 `status=2` 表鉴权失败）→ UrlExpired。
     * 注意与 R7 的 HTTP 5xx → Network 区分。
     */
    @Test
    fun throws_on_status_not_one() = runTest {
        val errorBody =
            """{"status":2,"url":["http://fs.open.kugou.com/abc.mp3"]}"""
        val resolver = KgUrlResolver(
            http = mockHttp(errorBody),
            crypto = createPlatformCrypto(),
        )
        assertFailsWith<MusicSourceException.UrlExpired> {
            resolver.resolve("abc123def456", Quality.K320)
        }
    }

    /**
     * R2: 针对已知 hash，`kgKey` 结果必须与 node 独立计算一致。
     *
     * 验证命令：
     * ```
     * node -e 'const crypto=require("crypto");
     *   console.log(crypto.createHash("md5")
     *     .update("abc123def456"+"kgcloudv2").digest("hex"));'
     * // => e2684c0c580f982233d38cce1a317b1e
     * ```
     */
    @Test
    fun md5_key_matches_community_vector() {
        val resolver = KgUrlResolver(
            http = mockHttp("""{}"""),
            crypto = createPlatformCrypto(),
        )
        assertEquals(
            "e2684c0c580f982233d38cce1a317b1e",
            resolver.kgKey("abc123def456"),
        )
    }

    /**
     * R2 补：大写的 hash 输入（真实调用里 songmid 常常是大写 hex）也要先 lowercase 再拼盐。
     */
    @Test
    fun md5_key_is_case_insensitive_on_input() {
        val resolver = KgUrlResolver(
            http = mockHttp("""{}"""),
            crypto = createPlatformCrypto(),
        )
        assertEquals(
            resolver.kgKey("abc123def456"),
            resolver.kgKey("ABC123DEF456"),
        )
    }

    /** R3: BR_MAP 不含 WAV → SourceDisabled。 */
    @Test
    fun unsupported_quality_throws_source_disabled() = runTest {
        val resolver = KgUrlResolver(
            http = mockHttp("""{}"""),
            crypto = createPlatformCrypto(),
        )
        assertFailsWith<MusicSourceException.SourceDisabled> {
            resolver.resolve("abc123def456", Quality.WAV)
        }
    }

    /**
     * R4: 抓请求，断言 host / path / method / 全部 query 形状。
     *
     * 特别断言 `hash` 是 songmid 的 uppercase，`key` 是 32 位小写 hex。
     */
    @Test
    fun outbound_request_matches_expected_shape() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val resolver = KgUrlResolver(
            http = mockHttpCapturing(captured, happyJson),
            crypto = createPlatformCrypto(),
        )
        val songmid = "abc123def456"
        resolver.resolve(songmid, Quality.K320)
        val req = captured.single()
        assertEquals("trackercdn.kugou.com", req.url.host)
        assertEquals("/i/v2/", req.url.encodedPath)
        assertEquals("GET", req.method.value)
        assertEquals("26", req.url.parameters["cmd"])

        val hashParam = req.url.parameters["hash"]
        assertEquals(songmid.uppercase(), hashParam)

        val keyParam = req.url.parameters["key"]
        assertTrue(
            keyParam != null && keyParam.length == 32 &&
                keyParam.all { it in '0'..'9' || it in 'a'..'f' },
            "key must be 32 hex lowercase chars, got: $keyParam",
        )

        assertEquals("6", req.url.parameters["pid"])
        assertEquals("play", req.url.parameters["behavior"])
        assertEquals("0", req.url.parameters["album_id"])
        assertEquals("0", req.url.parameters["album_audio_id"])
    }

    /** R7: HTTP 5xx → Network(code=500)，在解析前抛。 */
    @Test
    fun maps_5xx_to_network_exception() = runTest {
        val resolver = KgUrlResolver(
            http = mockHttp("", status = HttpStatusCode.InternalServerError),
            crypto = createPlatformCrypto(),
        )
        val ex = assertFailsWith<MusicSourceException.Network> {
            resolver.resolve("abc123def456", Quality.K320)
        }
        assertEquals(500, ex.code)
    }
}
