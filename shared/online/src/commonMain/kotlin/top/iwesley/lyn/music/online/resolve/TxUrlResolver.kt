package top.iwesley.lyn.music.online.resolve

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import top.iwesley.lyn.music.online.source.PlatformCrypto
import top.iwesley.lyn.music.online.types.PlayableUrl
import top.iwesley.lyn.music.online.types.Quality
import top.iwesley.lyn.music.scripting.MusicSourceException

/**
 * tx 源（QQ 音乐）URL 拿取器。
 *
 * 端点：https://u.y.qq.com/cgi-bin/musics.fcg
 * 签名：zzcSign（基于 SHA1，key 前缀 "zzcQQmusicAPIVersion2017"）
 * 依赖：PlatformCrypto.sha1 / hexEncode；DeviceFingerprint.guid
 *
 * 注意：M1.0 先用 SHA1 整体版本（不含 lx 原 pick/XOR/base64 拼接）。
 * 若 MockEngine 测试通过但真机请求被 403，则在 M1.0 RC 前补真实算法（见
 * vendor/lx-sdk/tx/utils/crypto.js 中的 zzcSign 实现）。
 */
class TxUrlResolver(
    private val http: HttpClient,
    private val crypto: PlatformCrypto,
    private val fingerprint: DeviceFingerprint,
) : SourceUrlResolver {

    companion object {
        private const val ENDPOINT = "https://u.y.qq.com/cgi-bin/musics.fcg"
        private const val ZZCSIGN_SALT = "zzcQQmusicAPIVersion2017"

        /**
         * 从 lx Quality 到 tx 文件名前缀（format）的映射。
         * 对照 vendor/lx-sdk/tx/musicInfo.js 中的 qualityMap。
         */
        private val QUALITY_TO_FORMAT = mapOf(
            "flac24bit" to "RS01",   // Hi-Res
            "flac" to "F000",        // 无损 FLAC
            "320k" to "M800",        // 320k
            "192k" to "M500",        // AAC 192k
            "128k" to "M500",        // 降级到 M500
        )
    }

    override suspend fun resolve(songmid: String, quality: Quality): PlayableUrl {
        val format = QUALITY_TO_FORMAT[quality.lxKey]
            ?: throw MusicSourceException.SourceDisabled("tx", "quality ${quality.lxKey} unsupported")
        val ext = if (format == "F000") "flac" else "mp3"
        val filename = "${format}${songmid}.${ext}"

        val payload = buildJsonObject {
            put("comm", buildJsonObject {
                put("uin", "0")
                put("format", "json")
                put("ct", 19)
                put("cv", 1873)
                put("guid", fingerprint.guid)
            })
            put("req_0", buildJsonObject {
                put("module", "vkey.GetVkeyServer")
                put("method", "CgiGetVkey")
                put("param", buildJsonObject {
                    put("guid", fingerprint.guid)
                    put("songmid", buildJsonArray { add(songmid) })
                    put("filename", buildJsonArray { add(filename) })
                    put("songtype", buildJsonArray { add(0) })
                    put("uin", "0")
                    put("loginflag", 1)
                    put("platform", "20")
                })
            })
        }.toString()

        val sign = zzcSign(payload)

        val resp = http.post(ENDPOINT) {
            url.parameters.append("sign", sign)
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        val text = resp.bodyAsText()
        val parsed = try {
            Json.parseToJsonElement(text).jsonObject
        } catch (e: Throwable) {
            throw MusicSourceException.Parse("tx", e)
        }

        val req0 = parsed["req_0"]?.jsonObject
            ?: throw MusicSourceException.Parse("tx", null)
        val data = req0["data"]?.jsonObject
            ?: throw MusicSourceException.Parse("tx", null)
        val sip = data["sip"]?.jsonArray?.firstOrNull()?.jsonPrimitive?.content
            ?: throw MusicSourceException.Parse("tx", null)
        val info = data["midurlinfo"]?.jsonArray?.firstOrNull()?.jsonObject
            ?: throw MusicSourceException.Parse("tx", null)
        val purl = info["purl"]?.jsonPrimitive?.content
            ?: throw MusicSourceException.UrlExpired("tx")
        if (purl.isEmpty()) throw MusicSourceException.UrlExpired("tx")

        return PlayableUrl(
            url = sip + purl,
            quality = quality,
            fetchedAt = kotlin.time.Clock.System.now(),
        )
    }

    /**
     * lx zzcSign 实际实现是 SHA1 + pick indices + XOR scramble + base64
     * （见 vendor/lx-sdk/tx/utils/crypto.js）。
     * M1.0 先用 SHA1 hex 整体版本（不含 pick/XOR/base64 拼接）。
     * 若 MockEngine 测试通过但真机请求被 403，则在 M1.0 RC 前补真实算法。
     */
    private fun zzcSign(payload: String): String {
        val salted = payload + ZZCSIGN_SALT
        val sha = crypto.sha1(salted.encodeToByteArray())
        return crypto.hexEncode(sha)
    }
}
