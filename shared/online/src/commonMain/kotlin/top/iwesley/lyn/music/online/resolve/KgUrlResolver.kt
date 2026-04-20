package top.iwesley.lyn.music.online.resolve

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import top.iwesley.lyn.music.online.source.OnlineJson
import top.iwesley.lyn.music.online.source.PlatformCrypto
import top.iwesley.lyn.music.online.types.PlayableUrl
import top.iwesley.lyn.music.online.types.Quality
import top.iwesley.lyn.music.scripting.MusicSourceException

/**
 * kg 源（酷狗）URL 拿取器。
 *
 * **来源诚实性**：lx kg vendor SDK 本身**不**实现 `getMusicUrl`
 * （`vendor/lx-sdk/kg/index.js:18-19` 把 `getMusicUrl` 路由到 lx 的 api-source 插件后端，
 * vendor 代码里没有真正的取链实现）。vendor 里存在的 `infSign` 函数是 callback 风格的
 * **排行/歌单**签名（`infSign(paramsObj, null, {callback(i){...}})`），与音频 URL 拿取无关。
 *
 * 本实现使用的 `http://trackercdn.kugou.com/i/v2/?cmd=26` 是**社区约定**（mitmproxy 抓酷狗
 * 官方客户端得到的路径），签名方案：`key = md5(hash.lowercase() + "kgcloudv2").hex_lower`，
 * 纯 Kotlin 即可实现，无需 JS 桥、无需 vendor infSign。
 *
 * **M1.0 限制**：不同音质需要不同 `cmd` 值（社区观察 hi-res/FLAC 走 `cmd=27`、普通走
 * `cmd=26`），M1.0 统一用 `cmd=26`，不按 `quality` 切换端点；[BR_MAP] 仅用于 gating
 * 受支持的 quality（不支持的抛 [MusicSourceException.SourceDisabled]）。响应拿回什么
 * 音质由服务端决定。M1.1 计划加入 cmd 切换。
 *
 * 端点形如：
 * ```
 * http://trackercdn.kugou.com/i/v2/?cmd=26&hash={HASH_UPPER}
 *   &key=md5({hash_lower}+"kgcloudv2")&pid=6&behavior=play&album_id=0&album_audio_id=0
 * ```
 *
 * 响应形如：`{"status":1,"url":["http://fs.open.kugou.com/..."]}`；
 * `status != 1` 或 `url` 数组缺失/为空 → [MusicSourceException.UrlExpired]。
 */
class KgUrlResolver(
    private val http: HttpClient,
    private val crypto: PlatformCrypto,
) : SourceUrlResolver {

    companion object {
        private const val ENDPOINT = "http://trackercdn.kugou.com/i/v2/"
        private const val KEY_SALT = "kgcloudv2"

        /**
         * lx [Quality.lxKey] → 酷狗码率 tag。M1.0 仅用于 gating（不支持的 quality 抛
         * [MusicSourceException.SourceDisabled]），不影响端点 URL。
         */
        private val BR_MAP = mapOf(
            "flac" to "flac",
            "320k" to "320",
            "192k" to "192",
            "128k" to "128",
        )
    }

    override suspend fun resolve(songmid: String, quality: Quality): PlayableUrl {
        BR_MAP[quality.lxKey]
            ?: throw MusicSourceException.SourceDisabled(
                "kg",
                "quality ${quality.lxKey} unsupported",
            )

        val hashUpper = songmid.uppercase()
        val key = kgKey(songmid)

        val resp = http.get(ENDPOINT) {
            url.parameters.append("cmd", "26")
            url.parameters.append("hash", hashUpper)
            url.parameters.append("key", key)
            url.parameters.append("pid", "6")
            url.parameters.append("behavior", "play")
            url.parameters.append("album_id", "0")
            url.parameters.append("album_audio_id", "0")
        }
        if (!resp.status.isSuccess()) {
            throw MusicSourceException.Network("kg", resp.status.value)
        }

        val text = resp.bodyAsText()
        val parsed = try {
            OnlineJson.parseToJsonElement(text).jsonObject
        } catch (e: Throwable) {
            throw MusicSourceException.Parse("kg", e)
        }

        val status = parsed["status"]?.jsonPrimitive?.intOrNull ?: parseErr("status")
        val urlArr = parsed["url"]?.jsonArray
        if (status != 1 || urlArr.isNullOrEmpty()) {
            throw MusicSourceException.UrlExpired("kg")
        }
        val playUrl = urlArr.first().jsonPrimitive.contentOrNull
            ?: throw MusicSourceException.UrlExpired("kg")

        return PlayableUrl(
            url = playUrl,
            quality = quality,
            fetchedAt = kotlin.time.Clock.System.now(),
        )
    }

    /**
     * `key = md5(hash.lowercase() + "kgcloudv2").hex_lower`。
     * 暴露为 `internal` 以便测试独立验证与 node 一行命令对齐（见
     * `KgUrlResolverTest.md5_key_matches_community_vector`）。
     */
    internal fun kgKey(hash: String): String =
        crypto.md5Hex((hash.lowercase() + KEY_SALT).encodeToByteArray())

    private fun parseErr(field: String): Nothing =
        throw MusicSourceException.Parse("kg", IllegalStateException("missing field: $field"))
}
