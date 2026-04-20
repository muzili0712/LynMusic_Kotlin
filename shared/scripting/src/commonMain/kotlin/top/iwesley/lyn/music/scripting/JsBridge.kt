package top.iwesley.lyn.music.scripting

/**
 * JS 脚本向宿主借用能力的契约。实际实现 (`JsBridgeImpl.*`) 在 T5 完成 —
 * T2 只落接口，方便 [JsRuntime] 的契约测试用 stub。
 */
interface JsBridge {
    suspend fun request(url: String, options: Map<String, JsValue>): JsValue

    fun md5(input: ByteArray): String
    fun sha1(input: ByteArray): ByteArray
    fun sha256(input: ByteArray): ByteArray

    fun aesEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray?, mode: String): ByteArray
    fun desEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray?, mode: String): ByteArray
    /**
     * RSA 公钥加密。padding 大小写不敏感：`"PKCS1"` 默认；`"NoPadding"` / `"None"` / `"Raw"` 走无填充（wy 源 `aesRsaEncrypt`）。
     * 其它值按 `"RSA/ECB/${padding}Padding"` 原样下发 JVM Cipher。
     */
    fun rsaEncrypt(data: ByteArray, publicKeyPem: String, padding: String = "PKCS1"): ByteArray

    fun base64Encode(input: ByteArray): String
    fun base64Decode(input: String): ByteArray

    fun bufferFrom(str: String, encoding: String): ByteArray
    fun bufferToString(bytes: ByteArray, encoding: String): String

    /**
     * zlib 解压。format 大小写不敏感：`"auto"`（默认，按 magic 推断）、`"zlib"`、`"raw"`（pako.inflateRaw）、`"gzip"`（pako.ungzip）。
     * kg 源在 JS 侧把三种 pako API 都映射到同一桥方法，用 format 区分。
     */
    fun zlibInflate(input: ByteArray, format: String = "auto"): ByteArray

    fun iconvDecode(input: ByteArray, encoding: String): String
    fun iconvEncode(input: String, encoding: String): ByteArray

    suspend fun setTimeout(delayMs: Long, callback: suspend () -> Unit): Long
    fun clearTimeout(id: Long)

    fun log(level: String, args: List<JsValue>)

    /** "android" | "jvm" | "ios" | "macos" */
    val platformTag: String
    val userAgent: String
}
