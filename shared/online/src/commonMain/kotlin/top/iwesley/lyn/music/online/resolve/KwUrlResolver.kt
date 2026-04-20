package top.iwesley.lyn.music.online.resolve

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import top.iwesley.lyn.music.online.source.OnlineJson
import top.iwesley.lyn.music.online.source.PlatformCrypto
import top.iwesley.lyn.music.online.types.PlayableUrl
import top.iwesley.lyn.music.online.types.Quality
import top.iwesley.lyn.music.scripting.MusicSourceException

/**
 * kw 源（酷我）URL 拿取器。
 *
 * **来源诚实性**：lx kw vendor SDK 本身**不**实现 `getMusicUrl`
 * （`vendor/lx-sdk/kw/index.js:62-63` 把音频 URL 查询路由到 lx 的 api-source 插件后端）。
 * 本实现使用的 `https://ac.kuwo.cn/url?type=convert_url3` 端点是**社区约定**
 * （mitmproxy 抓酷我官方客户端得到的 anti-ddos免签 路径），并非 vendor 代码。
 *
 * **路径取舍**：酷我官方客户端的"主"URL 路径走 `http://mobi.kuwo.cn/mobi.s` + DES 加密
 * 参数；本实现选用更简单的 `ac.kuwo.cn/url` anti-ddos 路径，绕过 DES 加密。
 * `mobi.s` DES 路径在 M1.0 不接入，预留到 M1.1 补。
 *
 * 端点形如：
 * ```
 * https://ac.kuwo.cn/url?format=mp3&rid={songmid}&response=url&type=convert_url3&br={br}kmp3
 * ```
 * `br` 由 [BR_MAP] 把 lx 的 `Quality.lxKey` 映射成酷我码率字串（flac→2000, 320k→320, ...）。
 *
 * 响应形如：`{"code":200,"url":"https://sycdn.kuwo.cn/abc.mp3"}`；
 * `code != 200` 或 `url` 缺失/空串 → [MusicSourceException.UrlExpired]。
 *
 * 构造参数 [crypto] 在本 resolver 实际未使用（kw anti-ddos 路径不签名），仅为
 * 与 [SourceUrlResolver] 其它实现保持构造接口统一。
 */
class KwUrlResolver(
    private val http: HttpClient,
    @Suppress("unused") private val crypto: PlatformCrypto,
) : SourceUrlResolver {

    companion object {
        private const val ENDPOINT = "https://ac.kuwo.cn/url"

        /** lx [Quality.lxKey] → 酷我 br 数值（拼到 `${br}kmp3`）。 */
        private val BR_MAP = mapOf(
            "flac" to "2000",
            "320k" to "320",
            "192k" to "192",
            "128k" to "128",
        )
    }

    override suspend fun resolve(songmid: String, quality: Quality): PlayableUrl {
        val br = BR_MAP[quality.lxKey]
            ?: throw MusicSourceException.SourceDisabled(
                "kw",
                "quality ${quality.lxKey} unsupported",
            )

        val resp = http.get(ENDPOINT) {
            url.parameters.append("format", "mp3")
            url.parameters.append("rid", songmid)
            url.parameters.append("response", "url")
            url.parameters.append("type", "convert_url3")
            url.parameters.append("br", "${br}kmp3")
        }
        if (!resp.status.isSuccess()) {
            throw MusicSourceException.Network("kw", resp.status.value)
        }

        val text = resp.bodyAsText()
        val parsed = try {
            OnlineJson.parseToJsonElement(text).jsonObject
        } catch (e: Throwable) {
            throw MusicSourceException.Parse("kw", e)
        }

        val code = parsed["code"]?.jsonPrimitive?.intOrNull ?: parseErr("code")
        val playUrl = parsed["url"]?.jsonPrimitive?.contentOrNull
        if (code != 200 || playUrl.isNullOrEmpty()) {
            throw MusicSourceException.UrlExpired("kw")
        }

        return PlayableUrl(
            url = playUrl,
            quality = quality,
            fetchedAt = kotlin.time.Clock.System.now(),
        )
    }

    private fun parseErr(field: String): Nothing =
        throw MusicSourceException.Parse("kw", IllegalStateException("missing field: $field"))
}
