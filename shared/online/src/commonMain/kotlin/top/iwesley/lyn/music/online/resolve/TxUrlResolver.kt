package top.iwesley.lyn.music.online.resolve

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import top.iwesley.lyn.music.online.source.OnlineJson
import top.iwesley.lyn.music.online.source.PlatformCrypto
import top.iwesley.lyn.music.online.types.PlayableUrl
import top.iwesley.lyn.music.online.types.Quality
import top.iwesley.lyn.music.scripting.MusicSourceException

/**
 * tx 源（QQ 音乐）URL 拿取器。
 *
 * 端点：https://u.y.qq.com/cgi-bin/musics.fcg
 * 签名：zzcSign —— 见 [zzcSign]（裸 SHA1(text) + pick/XOR/base64 scramble，无 salt）。
 * 依赖：PlatformCrypto.sha1 / hexEncode / base64Encode；DeviceFingerprint.guid。
 *
 * **关于 `format` → 扩展名映射的来源**：lx vendor 的 tx 源本身**不**实现 `getMusicUrl`
 * （它只做搜索 / 榜单 / 歌单等接口，音频 URL 交给 lx 的 api-source 插件后端）。
 * 这里使用的 format → ext 对照基于社区 QQ 音乐 API 惯例：
 *  - `RS01` → flac（Hi-Res 24bit）
 *  - `F000` → flac（无损 FLAC）
 *  - `M800` → mp3（320k）
 *  - `M500` → mp3（AAC 192k / 128k 回退）
 *  - `C400` → m4a（AAC，M1.0 未接入；预留到 M1.0 RC）
 *
 * 若真机端点在 M1.0 RC 回归 403，优先用 mitmproxy / Charles 抓 lx api-source 插件包
 * 再对齐，不要盲改算法。
 */
class TxUrlResolver(
    private val http: HttpClient,
    private val crypto: PlatformCrypto,
    private val fingerprint: DeviceFingerprint,
) : SourceUrlResolver {

    companion object {
        private const val ENDPOINT = "https://u.y.qq.com/cgi-bin/musics.fcg"

        /** lx Quality.lxKey → tx 文件名前缀（format）。 */
        private val QUALITY_TO_FORMAT = mapOf(
            "flac24bit" to "RS01",   // Hi-Res
            "flac" to "F000",        // 无损 FLAC
            "320k" to "M800",        // 320k
            "192k" to "M500",        // AAC 192k
            "128k" to "M500",        // 降级到 M500
        )

        /**
         * format → 文件扩展名。社区 QQ 音乐 API 惯例（见 KDoc 头）。
         *
         * 注：`C400` (AAC) 在 M1.0 未接入；预留到 M1.0 RC。
         */
        private val FORMAT_TO_EXT = mapOf(
            "RS01" to "flac",  // hi-res
            "F000" to "flac",
            "M800" to "mp3",
            "M500" to "mp3",
            // "C400" to "m4a",  // AAC — M1.0 not wired; reserved for M1.0 RC
        )

        // zzcSign pick/scramble 常量；见 vendor/lx-sdk/tx/utils/crypto.js:3-5
        private val PICK_PART_1 = intArrayOf(23, 14, 6, 36, 16, 40, 7, 19)
        private val PICK_PART_2 = intArrayOf(16, 1, 32, 12, 19, 27, 8, 5)
        private val SCRAMBLE = intArrayOf(
            89, 39, 179, 150, 218, 82, 58, 252, 177, 52,
            186, 123, 120, 64, 242, 133, 143, 161, 121, 179,
        )
    }

    override suspend fun resolve(songmid: String, quality: Quality): PlayableUrl {
        val format = QUALITY_TO_FORMAT[quality.lxKey]
            ?: throw MusicSourceException.SourceDisabled("tx", "quality ${quality.lxKey} unsupported")
        val ext = FORMAT_TO_EXT[format] ?: "mp3"
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
        if (!resp.status.isSuccess()) {
            throw MusicSourceException.Network("tx", resp.status.value)
        }
        val text = resp.bodyAsText()
        val parsed = try {
            OnlineJson.parseToJsonElement(text).jsonObject
        } catch (e: Throwable) {
            throw MusicSourceException.Parse("tx", e)
        }

        val req0 = parsed["req_0"]?.jsonObject ?: parseErr("req_0")
        val data = req0["data"]?.jsonObject ?: parseErr("req_0.data")
        val sip = data["sip"]?.jsonArray?.firstOrNull()?.jsonPrimitive?.content
            ?: parseErr("req_0.data.sip[0]")
        val info = data["midurlinfo"]?.jsonArray?.firstOrNull()?.jsonObject
            ?: parseErr("req_0.data.midurlinfo[0]")
        val purl = info["purl"]?.jsonPrimitive?.content
            ?: throw MusicSourceException.UrlExpired("tx")
        if (purl.isEmpty()) throw MusicSourceException.UrlExpired("tx")

        return PlayableUrl(
            url = sip + purl,
            quality = quality,
            fetchedAt = kotlin.time.Clock.System.now(),
        )
    }

    private fun parseErr(field: String): Nothing =
        throw MusicSourceException.Parse("tx", IllegalStateException("missing field: $field"))

    /**
     * lx zzcSign（见 `vendor/lx-sdk/tx/utils/crypto.js:17-24`）：
     *
     * ```
     * hash = SHA1(text)                                 // hex string (40 chars, lowercase)
     * part1 = pick(hash, [23,14,6,36,16,40,7,19])       // 8 chars
     * part2 = pick(hash, [16,1,32,12,19,27,8,5])        // 8 chars
     * part3[i] = SCRAMBLE[i] XOR int(hash[2i..2i+2], 16) for i in 0..19   // 20 bytes
     * b64   = base64(part3).replace(/[\\/+=]/g, '')
     * return "zzc${part1}${b64}${part2}".toLowerCase()
     * ```
     *
     * 实现注意事项：
     *  - **输入是裸 payload，不预先加任何 salt**。旧实现里用的
     *    `"zzcQQmusicAPIVersion2017"` 是虚构常量，vendor 里不存在，已移除。
     *  - SHA1 摘要**以 hex 字符串形式**做 pick 和字节提取（不是原始 byte[]）。
     *  - [PICK_PART_1] 的最后一个索引是 `40`。hex 字符串长度恰好 40，所以 `hash[40]`
     *    在 lx JS 里是 `undefined`，`indexes.map(i => hash[i]).join('')` 会把
     *    `undefined` 拼成空字符串。Kotlin 侧必须用 `getOrNull(it)?.toString() ?: ""`
     *    才能逐字节匹配 lx 输出；若用 `hash[40]` 会抛 IndexOutOfBounds。
     *  - XOR 源是 `parseInt(hash.slice(2i, 2i+2), 16)`，即把 hex 重新解析成字节。
     *    本实现直接用 SHA1 原始 byte[] 里对应位置的字节，二者数值等价。
     *  - base64 里 `\`、`/`、`+`、`=` 全部清洗掉（lx 在 URL query 里用）。
     *
     * 参考测试向量（`TxUrlResolverTest.zzcSign_matches_lx_vendor_for_known_input`）：
     *  - `zzcSign("hello")` = `"zzcfa14dde89n1iwax0l5rimr0qwjexceiov4daaee8d6"`
     *
     * 该向量由 `node -e '...vendor algo...'` 预先跑 lx JS 实现生成，详见测试文件注释。
     */
    internal fun zzcSign(text: String): String {
        val hashBytes = crypto.sha1(text.encodeToByteArray())
        val hashHex = crypto.hexEncode(hashBytes)
        val part1 = PICK_PART_1.joinToString("") { hashHex.getOrNull(it)?.toString() ?: "" }
        val part2 = PICK_PART_2.joinToString("") { hashHex.getOrNull(it)?.toString() ?: "" }
        val part3 = ByteArray(SCRAMBLE.size) { i ->
            (SCRAMBLE[i] xor (hashBytes[i].toInt() and 0xFF)).toByte()
        }
        val b64 = crypto.base64Encode(part3).replace(Regex("[\\\\/+=]"), "")
        return "zzc$part1$b64$part2".lowercase()
    }
}
