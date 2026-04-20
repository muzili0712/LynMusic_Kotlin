package top.iwesley.lyn.music.online.resolve

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
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
 * mg 源（咪咕）URL 拿取器。
 *
 * **来源诚实性**：lx mg vendor SDK 本身**不**实现 `getMusicUrl`
 * （`vendor/lx-sdk/mg/index.js:18-19` 把 `getMusicUrl` 路由到 lx 的 api-source 插件后端，
 * vendor 代码里没有真正的取链实现）。vendor 的 `musicInfo.js:13` 使用
 * `https://c.musicapp.migu.cn/.../resourceinfo.do?resourceType=2` 的 **POST form** 变体，
 * 需要 session cookie / 鉴权。本实现使用的 `app.c.nf.migu.cn/.../resourceinfo.do?by=orpheus`
 * 是**社区免签 GET 端点**，只需带 MiguMusic 客户端 UA 即可，避免 session 依赖。
 *
 * 签名方案：**无**（`by=orpheus` 端点本身免签；[crypto] 参数保留是为了
 * [SourceUrlResolver] 接口一致性，未在本类内使用）。
 *
 * quality → formatType 映射（[CODE_MAP]）：flac24bit/ZQ, flac/SQ, 320k/HQ, 128k/PQ。
 * mg 没有 192k 码率层，[Quality.K192] → [MusicSourceException.SourceDisabled]。
 *
 * 响应形如：
 * ```
 * {"resource":[{"newRateFormats":[{"formatType":"HQ","url":"http://aod.migu.cn/...",
 *   "androidUrl":"..."}]}]}
 * ```
 * 兼容旧响应：`newRateFormats` 缺失时回退到同级的 `rateFormats`。
 * 每个 entry 里 `url` 优先，缺则用 `androidUrl` 兜底；`//` 开头按 `http:` 拼回。
 */
class MgUrlResolver(
    private val http: HttpClient,
    @Suppress("unused") private val crypto: PlatformCrypto,
) : SourceUrlResolver {

    companion object {
        private const val ENDPOINT =
            "http://app.c.nf.migu.cn/MIGUM2.0/v1.0/content/resourceinfo.do"
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 11) MiguMusic/7.7.2"

        /**
         * lx [Quality.lxKey] → mg `formatType` 码率 tag。mg 没有 192k，
         * [Quality.K192] 会在 [MgUrlResolver.resolve] 里抛
         * [MusicSourceException.SourceDisabled]。
         */
        private val CODE_MAP = mapOf(
            "flac24bit" to "ZQ",
            "flac" to "SQ",
            "320k" to "HQ",
            "128k" to "PQ",
        )
    }

    override suspend fun resolve(songmid: String, quality: Quality): PlayableUrl {
        val code = CODE_MAP[quality.lxKey]
            ?: throw MusicSourceException.SourceDisabled(
                "mg",
                "quality ${quality.lxKey} unsupported",
            )

        val resp = http.get(ENDPOINT) {
            url.parameters.append("resourceId", songmid)
            url.parameters.append("resourceType", "2")
            url.parameters.append("by", "orpheus")
            header("User-Agent", USER_AGENT)
        }
        if (!resp.status.isSuccess()) {
            throw MusicSourceException.Network("mg", resp.status.value)
        }

        val text = resp.bodyAsText()
        val parsed = try {
            OnlineJson.parseToJsonElement(text).jsonObject
        } catch (e: Throwable) {
            throw MusicSourceException.Parse("mg", e)
        }

        val resources = parsed["resource"]?.jsonArray ?: parseErr("resource")
        val first = resources.firstOrNull()?.jsonObject ?: parseErr("resource[0]")
        val rateformats = first["newRateFormats"]?.jsonArray
            ?: first["rateFormats"]?.jsonArray
            ?: parseErr("newRateFormats | rateFormats")
        val match = rateformats.firstOrNull {
            it.jsonObject["formatType"]?.jsonPrimitive?.contentOrNull == code
        }?.jsonObject ?: throw MusicSourceException.UrlExpired("mg")
        val rawUrl = match["url"]?.jsonPrimitive?.contentOrNull
            ?: match["androidUrl"]?.jsonPrimitive?.contentOrNull
            ?: throw MusicSourceException.UrlExpired("mg")

        val playUrl = if (rawUrl.startsWith("//")) "http:$rawUrl" else rawUrl

        return PlayableUrl(
            url = playUrl,
            quality = quality,
            fetchedAt = kotlin.time.Clock.System.now(),
        )
    }

    private fun parseErr(field: String): Nothing =
        throw MusicSourceException.Parse("mg", IllegalStateException("missing field: $field"))
}
