package top.iwesley.lyn.music.online.source

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import kotlinx.coroutines.delay
import top.iwesley.lyn.music.core.model.DiagnosticLogLevel
import top.iwesley.lyn.music.core.model.GlobalDiagnosticLogger
import top.iwesley.lyn.music.online.diagnostics.OnlineLogTags
import top.iwesley.lyn.music.scripting.JsBridge
import top.iwesley.lyn.music.scripting.JsValue

/**
 * 默认 JS ↔ 宿主桥实现。
 *
 * 设计点：
 *  - 网络：单一 [io.ktor.client.HttpClient]，method/headers/body 从 JS 传入的 options 解析；响应统一返回
 *    `{ status, statusMessage, headers, body, url }` 的 [JsValue.Obj]。
 *  - 加解密：所有 crypto 路径委派到 [PlatformCrypto]（expect/actual），JVM/Android 用 javax.crypto，
 *    Apple 按 M0 约定仅落 MD5/SHA1/SHA256/AES/base64/zlib/iconv，DES/RSA 抛 NotImplementedError。
 *  - setTimeout：M0 仅 fire-and-forget；没做真正的 id 管理（lx 源里 setInterval 只用于 Promise 轮询，不 clear 安全）。
 *
 * **不要**直接 new；构造由 `DefaultMusicSourceFacade.build(bridgeFactory = ...)` 注入。
 * 五源共享同一个 HttpClient 即可（每条请求独立，不存跨请求 state）。
 */
class JsBridgeImpl(
    private val http: HttpClient,
    override val platformTag: String,
    override val userAgent: String,
    private val crypto: PlatformCrypto,
) : JsBridge {

    override suspend fun request(url: String, options: Map<String, JsValue>): JsValue {
        val method = (options["method"] as? JsValue.Str)?.value ?: "GET"
        val headers: Map<String, String> = ((options["headers"] as? JsValue.Obj)?.entries.orEmpty())
            .mapValues { (_, v) -> (v as? JsValue.Str)?.value.orEmpty() }
        val body = (options["body"] as? JsValue.Str)?.value

        val resp = http.request(url) {
            this.method = HttpMethod.parse(method.uppercase())
            headers.forEach { (k, v) -> header(k, v) }
            if (headers.keys.none { it.equals(HttpHeaders.UserAgent, ignoreCase = true) }) {
                header(HttpHeaders.UserAgent, userAgent)
            }
            if (body != null) setBody(body)
        }
        val text = resp.bodyAsText()
        val respHeaders = resp.headers.entries().associate { (name, values) ->
            name to JsValue.Str(values.joinToString(","))
        }
        return JsValue.Obj(
            mapOf(
                "status" to JsValue.Num(resp.status.value.toDouble()),
                "statusMessage" to JsValue.Str(resp.status.description),
                "headers" to JsValue.Obj(respHeaders),
                "body" to JsValue.Str(text),
                "url" to JsValue.Str(resp.call.request.url.toString()),
            ),
        )
    }

    override fun md5(input: ByteArray): String = crypto.md5Hex(input)
    override fun sha1(input: ByteArray): ByteArray = crypto.sha1(input)
    override fun sha256(input: ByteArray): ByteArray = crypto.sha256(input)
    override fun aesEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray?, mode: String): ByteArray =
        crypto.aesEncrypt(data, key, iv, mode)
    override fun desEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray?, mode: String): ByteArray =
        crypto.desEncrypt(data, key, iv, mode)
    override fun rsaEncrypt(data: ByteArray, publicKeyPem: String): ByteArray =
        crypto.rsaEncrypt(data, publicKeyPem)

    override fun base64Encode(input: ByteArray): String = crypto.base64Encode(input)
    override fun base64Decode(input: String): ByteArray = crypto.base64Decode(input)

    override fun bufferFrom(str: String, encoding: String): ByteArray =
        when (encoding.lowercase()) {
            "utf-8", "utf8", "ascii", "binary" -> str.encodeToByteArray()
            "hex" -> crypto.hexDecode(str)
            "base64" -> crypto.base64Decode(str)
            else -> str.encodeToByteArray()
        }

    override fun bufferToString(bytes: ByteArray, encoding: String): String =
        when (encoding.lowercase()) {
            "utf-8", "utf8", "ascii", "binary" -> bytes.decodeToString()
            "hex" -> crypto.hexEncode(bytes)
            "base64" -> crypto.base64Encode(bytes)
            else -> bytes.decodeToString()
        }

    override fun zlibInflate(input: ByteArray): ByteArray = crypto.zlibInflate(input)
    override fun iconvDecode(input: ByteArray, encoding: String): String = crypto.iconvDecode(input, encoding)
    override fun iconvEncode(input: String, encoding: String): ByteArray = crypto.iconvEncode(input, encoding)

    override suspend fun setTimeout(delayMs: Long, callback: suspend () -> Unit): Long {
        delay(delayMs)
        callback()
        return 0L // M0：不做 id 管理；clearTimeout 是 no-op，lx setInterval 仅 Promise 轮询，无害。
    }
    override fun clearTimeout(id: Long) = Unit

    override fun log(level: String, args: List<JsValue>) {
        val lvl = when (level.lowercase()) {
            "debug" -> DiagnosticLogLevel.DEBUG
            "info" -> DiagnosticLogLevel.INFO
            "warn", "warning" -> DiagnosticLogLevel.WARN
            "error" -> DiagnosticLogLevel.ERROR
            else -> DiagnosticLogLevel.DEBUG
        }
        GlobalDiagnosticLogger.log(
            level = lvl,
            tag = OnlineLogTags.BRIDGE,
            message = args.joinToString(" ") { it.renderForLog() },
            throwable = null,
        )
    }
}

private fun JsValue.renderForLog(): String = when (this) {
    is JsValue.Null -> "null"
    is JsValue.Undefined -> "undefined"
    is JsValue.Bool -> value.toString()
    is JsValue.Num -> value.toString()
    is JsValue.Str -> value
    is JsValue.Bytes -> "<bytes:${data.size}>"
    is JsValue.Arr -> items.joinToString(",", "[", "]") { it.renderForLog() }
    is JsValue.Obj -> entries.entries.joinToString(",", "{", "}") { (k, v) -> "$k=${v.renderForLog()}" }
}

/**
 * 纯 compute 的平台能力契约。所有方法都是同步且线程安全；
 * 各 actual 实现用平台原生 crypto（JVM/Android: javax.crypto；Apple: krypto 或占位）。
 *
 * 注：iconv 编解码在 JVM/Android 上用 `java.nio.charset.Charset`；
 * Apple 上走 NSString 编码族，未识别编码统一退化成 UTF-8。
 */
interface PlatformCrypto {
    fun md5Hex(input: ByteArray): String
    fun sha1(input: ByteArray): ByteArray
    fun sha256(input: ByteArray): ByteArray
    fun aesEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray?, mode: String): ByteArray
    fun desEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray?, mode: String): ByteArray
    fun rsaEncrypt(data: ByteArray, publicKeyPem: String): ByteArray
    fun base64Encode(input: ByteArray): String
    fun base64Decode(input: String): ByteArray
    fun hexEncode(input: ByteArray): String
    fun hexDecode(input: String): ByteArray
    fun zlibInflate(input: ByteArray): ByteArray
    fun iconvDecode(input: ByteArray, encoding: String): String
    fun iconvEncode(input: String, encoding: String): ByteArray
}

/** 平台专属 [PlatformCrypto] 工厂；由 commonMain 侧的 [DefaultMusicSourceFacade] 委派调用。 */
expect fun createPlatformCrypto(): PlatformCrypto
