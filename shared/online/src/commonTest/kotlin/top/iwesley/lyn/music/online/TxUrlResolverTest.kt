package top.iwesley.lyn.music.online

import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import top.iwesley.lyn.music.online.resolve.DeviceFingerprint
import top.iwesley.lyn.music.online.resolve.TxUrlResolver
import top.iwesley.lyn.music.online.source.createPlatformCrypto
import top.iwesley.lyn.music.online.types.Quality
import top.iwesley.lyn.music.scripting.MusicSourceException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TxUrlResolverTest {

    private val happyJson =
        """{"code":0,"req_0":{"data":{"sip":["https://ws.stream.qqmusic.qq.com/"],"midurlinfo":[{"purl":"C400xxxxxx.m4a?guid=a","filename":"C400xxxxxx.m4a"}]}}}"""

    private val fakeFingerprint = DeviceFingerprint(
        guid = "a".repeat(32),
        wid = "b".repeat(32),
        deviceId = "c".repeat(32),
    )

    @Test
    fun resolves_tx_url_for_k320() = runTest {
        val resolver = TxUrlResolver(
            http = mockHttp(happyJson),
            crypto = createPlatformCrypto(),
            fingerprint = fakeFingerprint,
        )
        val url = resolver.resolve("xxxxxx", Quality.K320)
        assertTrue(url.url.startsWith("https://ws.stream.qqmusic.qq.com/C400xxxxxx.m4a"))
        assertEquals(Quality.K320, url.quality)
    }

    @Test
    fun throws_on_empty_purl() = runTest {
        val mockResponse =
            """{"code":0,"req_0":{"data":{"sip":["https://ws.stream.qqmusic.qq.com/"],"midurlinfo":[{"purl":"","filename":""}]}}}"""
        val resolver = TxUrlResolver(
            http = mockHttp(mockResponse),
            crypto = createPlatformCrypto(),
            fingerprint = fakeFingerprint,
        )
        assertFailsWith<MusicSourceException> {
            resolver.resolve("xxxxxx", Quality.K320)
        }
    }

    /**
     * C1 确定性向量：对 "hello" 跑 lx vendor 的 zzcSign 应得到固定输出。
     *
     * 期望值由 `vendor/lx-sdk/tx/utils/crypto.js` 同算法的 Node.js 复现脚本生成：
     * ```bash
     * node -e '
     *   const crypto = require("crypto");
     *   const P1 = [23,14,6,36,16,40,7,19], P2 = [16,1,32,12,19,27,8,5];
     *   const SC = [89,39,179,150,218,82,58,252,177,52,186,123,120,64,242,133,143,161,121,179];
     *   const h = crypto.createHash("sha1").update("hello").digest("hex");
     *   const p1 = P1.map(i => h[i]).join(""), p2 = P2.map(i => h[i]).join("");
     *   const p3 = SC.map((v,i) => v ^ parseInt(h.slice(i*2,i*2+2),16));
     *   const b = Buffer.from(p3).toString("base64").replace(/[\\/+=]/g,"");
     *   console.log(`zzc${p1}${b}${p2}`.toLowerCase());
     * '
     * // -> zzcfa14dde89n1iwax0l5rimr0qwjexceiov4daaee8d6
     * ```
     *
     * "hello" 的 SHA1 hex 是 `aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d`（40 字符），
     * PICK_PART_1 的索引 40 越界 → lx JS 返回 undefined，join 成空字符串；
     * Kotlin 侧必须用 `getOrNull` 才能逐字节匹配。
     */
    @Test
    fun zzcSign_matches_lx_vendor_for_known_input() = runTest {
        val resolver = TxUrlResolver(
            http = mockHttp("""{"code":0}"""),
            crypto = createPlatformCrypto(),
            fingerprint = fakeFingerprint,
        )
        val expected = "zzcfa14dde89n1iwax0l5rimr0qwjexceiov4daaee8d6"
        assertEquals(expected, resolver.zzcSign("hello"))
    }

    /** C2: RS01 / F000 用 .flac；M800 / M500 用 .mp3。 */
    @Test
    fun filename_for_flac24bit_uses_flac_extension() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val resolver = TxUrlResolver(
            http = mockHttpCapturing(captured, happyJson),
            crypto = createPlatformCrypto(),
            fingerprint = fakeFingerprint,
        )
        resolver.resolve("abc", Quality.FLAC24BIT)
        val body = bodyText(captured.single())
        assertTrue(
            body.contains("\"RS01abc.flac\""),
            "expected RS01abc.flac in body, got: $body",
        )
    }

    /** I1: 捕获请求，断言端点 / method / sign / body 结构（以 K320 为例）。 */
    @Test
    fun outbound_request_matches_expected_shape() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val resolver = TxUrlResolver(
            http = mockHttpCapturing(captured, happyJson),
            crypto = createPlatformCrypto(),
            fingerprint = fakeFingerprint,
        )
        resolver.resolve("xxxxxx", Quality.K320)
        val req = captured.single()
        assertEquals("u.y.qq.com", req.url.host)
        assertEquals("/cgi-bin/musics.fcg", req.url.encodedPath)
        assertEquals("POST", req.method.value)

        val sign = req.url.parameters["sign"]
        assertNotNull(sign, "sign parameter must be present")
        assertTrue(sign.startsWith("zzc"), "sign must start with zzc, got: $sign")

        val body = bodyText(req)
        assertTrue(body.contains("\"module\":\"vkey.GetVkeyServer\""), "body missing module: $body")
        assertTrue(body.contains("\"method\":\"CgiGetVkey\""), "body missing method: $body")
        assertTrue(body.contains("\"songmid\":[\"xxxxxx\"]"), "body missing songmid: $body")
        assertTrue(body.contains("\"filename\":[\"M800xxxxxx.mp3\"]"), "body missing filename: $body")
    }

    /** I4: 5xx → Network(code)。 */
    @Test
    fun maps_5xx_to_network_exception() = runTest {
        val resolver = TxUrlResolver(
            http = mockHttp("", status = HttpStatusCode.InternalServerError),
            crypto = createPlatformCrypto(),
            fingerprint = fakeFingerprint,
        )
        val ex = assertFailsWith<MusicSourceException.Network> {
            resolver.resolve("xxxxxx", Quality.K320)
        }
        assertEquals(500, ex.code)
    }

    /** Additional coverage: 不支持的音质（WAV）→ SourceDisabled。 */
    @Test
    fun unsupported_quality_throws_source_disabled() = runTest {
        val resolver = TxUrlResolver(
            http = mockHttp("""{}"""),
            crypto = createPlatformCrypto(),
            fingerprint = fakeFingerprint,
        )
        assertFailsWith<MusicSourceException.SourceDisabled> {
            resolver.resolve("x", Quality.WAV)
        }
    }
}
