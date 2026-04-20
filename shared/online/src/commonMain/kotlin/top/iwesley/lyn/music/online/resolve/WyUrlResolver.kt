package top.iwesley.lyn.music.online.resolve

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import top.iwesley.lyn.music.online.source.OnlineJson
import top.iwesley.lyn.music.online.source.PlatformCrypto
import top.iwesley.lyn.music.online.types.PlayableUrl
import top.iwesley.lyn.music.online.types.Quality
import top.iwesley.lyn.music.scripting.MusicSourceException

/**
 * wy 源（网易云）URL 拿取器。
 *
 * **来源诚实性**：lx wy vendor SDK 本身**不**实现 `getMusicUrl`，它把音频 URL 查询
 * 路由到 lx 的 api-source 插件后端。`eapi` 加密算法**确实**在 vendor
 * `utils/crypto.js:48-56` 里（[`eapiEncrypt`] 完全复刻该实现），但 M1.0 选用的
 * `/eapi/song/enhance/player/url` 端点及 `params={"ids":"[mid]","br":N}` 请求体
 * 形状是**社区约定**（NeteaseCloudMusicApi `module/crypto.js`），非 vendor 代码。
 *
 * **Padding 选择（与 vendor 的差异）**：vendor 用 `AES_MODE.ECB_128_NoPadding`，
 * 但 eapi 拼出来的明文 `{url}-36cd479b6b5-{text}-36cd479b6b5-{md5_32hex}` 几乎永远
 * 不是 16 字节对齐的。React Native native AES 模块常在 unaligned 输入上静默套 PKCS7，
 * 但 Kotlin JCE 的 `AES/ECB/NoPadding` 会直接抛 `IllegalBlockSizeException`。
 * 因此改用 `"ECB/PKCS7Padding"`，这也是 NeteaseCloudMusicApi 社区实现的选择，
 * 服务端接受（两种输出前 N-1 个 block 完全一致，仅最后 padding block 不同）。
 *
 * 响应形状：`{"code":200,"data":[{"id":12345,"url":"http://...","br":320000,...}]}`；
 * `url` 缺失或 `null` → [MusicSourceException.UrlExpired]。
 */
class WyUrlResolver(
    private val http: HttpClient,
    private val crypto: PlatformCrypto,
) : SourceUrlResolver {

    companion object {
        private const val ENDPOINT = "http://interface3.music.163.com/eapi/song/enhance/player/url"
        private const val EAPI_KEY = "e82ckenh8dichen8" // 16 bytes raw
        private const val EAPI_URL_PATH = "/api/song/enhance/player/url"

        /** lx Quality.lxKey → wy 码率（bps）。 */
        private val BR_MAP = mapOf(
            "flac" to 999000,
            "320k" to 320000,
            "192k" to 192000,
            "128k" to 128000,
        )
    }

    override suspend fun resolve(songmid: String, quality: Quality): PlayableUrl {
        val br = BR_MAP[quality.lxKey]
            ?: throw MusicSourceException.SourceDisabled(
                "wy",
                "quality ${quality.lxKey} unsupported",
            )
        val payload = """{"ids":"[$songmid]","br":$br}"""
        val params = eapiEncrypt(EAPI_URL_PATH, payload)

        val resp = http.post(ENDPOINT) {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("params=$params")
        }
        if (!resp.status.isSuccess()) {
            throw MusicSourceException.Network("wy", resp.status.value)
        }
        val text = resp.bodyAsText()
        val parsed = try {
            OnlineJson.parseToJsonElement(text).jsonObject
        } catch (e: Throwable) {
            throw MusicSourceException.Parse("wy", e)
        }
        val dataArr = parsed["data"]?.jsonArray ?: parseErr("data")
        val first = dataArr.firstOrNull()?.jsonObject ?: parseErr("data[0]")
        val url = first["url"]?.jsonPrimitive?.contentOrNull
            ?: throw MusicSourceException.UrlExpired("wy")
        if (url.isEmpty()) throw MusicSourceException.UrlExpired("wy")

        return PlayableUrl(
            url = url,
            quality = quality,
            fetchedAt = kotlin.time.Clock.System.now(),
        )
    }

    /**
     * lx eapi（见 `vendor/lx-sdk/wy/utils/crypto.js:48-56`）：
     *
     * ```
     * message = "nobody" + url + "use" + data + "md5forencrypt"
     * digest  = md5_hex_lowercase(message)
     * plain   = url + "-36cd479b6b5-" + data + "-36cd479b6b5-" + digest
     * cipher  = AES-128-ECB-PKCS7Padding(plain_utf8, key="e82ckenh8dichen8")
     * return hex(cipher).uppercase()
     * ```
     *
     * 对比 vendor：vendor 传 `"ECB_128_NoPadding"`，本实现传 `"ECB/PKCS7Padding"`
     * （理由见类 KDoc）。
     */
    internal fun eapiEncrypt(url: String, data: String): String {
        val message = "nobody${url}use${data}md5forencrypt"
        val digest = crypto.md5Hex(message.encodeToByteArray())
        val plain = "$url-36cd479b6b5-$data-36cd479b6b5-$digest"
        val encrypted = crypto.aesEncrypt(
            data = plain.encodeToByteArray(),
            key = EAPI_KEY.encodeToByteArray(),
            iv = null,
            mode = "ECB/PKCS7Padding",
        )
        return crypto.hexEncode(encrypted).uppercase()
    }

    private fun parseErr(field: String): Nothing =
        throw MusicSourceException.Parse("wy", IllegalStateException("missing field: $field"))
}
