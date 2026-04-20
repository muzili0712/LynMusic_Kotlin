package top.iwesley.lyn.music.online

import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import top.iwesley.lyn.music.online.resolve.WyUrlResolver
import top.iwesley.lyn.music.online.source.createPlatformCrypto
import top.iwesley.lyn.music.online.types.Quality
import top.iwesley.lyn.music.scripting.MusicSourceException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class WyUrlResolverTest {

    private val happyJson =
        """{"code":200,"data":[{"id":12345,"url":"http://m10.music.126.net/abc.mp3","br":320000,"size":1234567}]}"""

    @Test
    fun resolves_wy_url_for_k320() = runTest {
        val resolver = WyUrlResolver(
            http = mockHttp(happyJson),
            crypto = createPlatformCrypto(),
        )
        val url = resolver.resolve("12345", Quality.K320)
        assertTrue(
            url.url.startsWith("http://m10.music.126.net/"),
            "unexpected url: ${url.url}",
        )
        assertEquals(Quality.K320, url.quality)
    }

    @Test
    fun throws_on_missing_url() = runTest {
        val mockResponse = """{"code":200,"data":[{"id":12345,"url":null}]}"""
        val resolver = WyUrlResolver(
            http = mockHttp(mockResponse),
            crypto = createPlatformCrypto(),
        )
        assertFailsWith<MusicSourceException.UrlExpired> {
            resolver.resolve("12345", Quality.K320)
        }
    }

    /** R3: BR_MAP 不含 WAV → SourceDisabled。 */
    @Test
    fun unsupported_quality_throws_source_disabled() = runTest {
        val resolver = WyUrlResolver(
            http = mockHttp("""{}"""),
            crypto = createPlatformCrypto(),
        )
        assertFailsWith<MusicSourceException.SourceDisabled> {
            resolver.resolve("12345", Quality.WAV)
        }
    }

    /**
     * R2 确定性向量：对固定 url+text 走 eapi 加密应得到固定 hex-upper 密文。
     *
     * 期望值由 Node 23.x 复现脚本生成（aes-128-ecb + setAutoPadding(true) 等价
     * PKCS7；见 [WyUrlResolver.eapiEncrypt] KDoc 里"Padding 选择"一段）：
     *
     * ```bash
     * node -e '
     *   const crypto = require("crypto");
     *   const url = "/api/song/enhance/player/url";
     *   const text = `{"ids":"[12345]","br":320000}`;
     *   const message = `nobody${url}use${text}md5forencrypt`;
     *   const md5 = crypto.createHash("md5").update(message).digest("hex");
     *   const data = `${url}-36cd479b6b5-${text}-36cd479b6b5-${md5}`;
     *   const cipher = crypto.createCipheriv("aes-128-ecb", "e82ckenh8dichen8", null);
     *   cipher.setAutoPadding(true); // PKCS7
     *   const enc = Buffer.concat([cipher.update(data, "utf8"), cipher.final()]);
     *   console.log(enc.toString("hex").toUpperCase());
     * '
     * // -> FA90B329E9614F79E79598F37DC2EDB4...5594731 (256 hex chars, 128 bytes = 8 AES blocks)
     * ```
     */
    @Test
    fun eapi_matches_community_vector_for_known_input() = runTest {
        val resolver = WyUrlResolver(
            http = mockHttp("""{}"""),
            crypto = createPlatformCrypto(),
        )
        val expected =
            "FA90B329E9614F79E79598F37DC2EDB430F8378D2A2796338F0BFDEAEF824A22" +
                "975CDA9D96D79E6DC4A59218CDB8199FF7384F9A42DF2DF40BFE657260DE9862" +
                "8D532D31B2338802ADDEFFC5550DEE992E9F87F422D284C33AFD1C5DD52856D2" +
                "11D28328BC9F6D6E31B7469F72C2217995D550029E54ED9FE5AF88D6F5594731"
        val actual = resolver.eapiEncrypt(
            url = "/api/song/enhance/player/url",
            data = """{"ids":"[12345]","br":320000}""",
        )
        assertEquals(expected, actual)
    }

    /** R4: 抓请求，断言 host / path / method / body 形状 / content-type。 */
    @Test
    fun outbound_request_matches_expected_shape() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val resolver = WyUrlResolver(
            http = mockHttpCapturing(captured, happyJson),
            crypto = createPlatformCrypto(),
        )
        resolver.resolve("12345", Quality.K320)
        val req = captured.single()
        assertEquals("interface3.music.163.com", req.url.host)
        assertEquals("/eapi/song/enhance/player/url", req.url.encodedPath)
        assertEquals("POST", req.method.value)

        val body = bodyText(req)
        assertTrue(body.startsWith("params="), "body must start with 'params=', got: $body")
        val hex = body.removePrefix("params=")
        assertTrue(
            hex.matches(Regex("^[0-9A-F]+$")),
            "body after 'params=' must be uppercase hex, got: $hex",
        )
        // 明文长度 (url 28 + separators 26 + text 31 + md5 32) = 117 bytes → PKCS7 → 128 bytes → 256 hex chars
        // 仅检查 >= 32 字节（2 AES block，最小健壮底线），避免对明文长度做硬编码依赖。
        assertTrue(hex.length >= 64, "cipher hex too short: len=${hex.length}")
        assertTrue(hex.length % 32 == 0, "cipher hex length must be multiple of 32 (AES block): len=${hex.length}")

        val contentType = req.body.contentType?.toString().orEmpty()
        assertTrue(
            contentType.startsWith("application/x-www-form-urlencoded"),
            "expected form-urlencoded content type, got: $contentType",
        )
    }

    /** R7: 5xx → Network(code=500)，在解析前就该抛。 */
    @Test
    fun maps_5xx_to_network_exception() = runTest {
        val resolver = WyUrlResolver(
            http = mockHttp("", status = HttpStatusCode.InternalServerError),
            crypto = createPlatformCrypto(),
        )
        val ex = assertFailsWith<MusicSourceException.Network> {
            resolver.resolve("12345", Quality.K320)
        }
        assertEquals(500, ex.code)
    }
}
