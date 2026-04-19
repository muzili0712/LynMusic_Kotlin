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
    fun rsaEncrypt(data: ByteArray, publicKeyPem: String): ByteArray

    fun base64Encode(input: ByteArray): String
    fun base64Decode(input: String): ByteArray

    fun bufferFrom(str: String, encoding: String): ByteArray
    fun bufferToString(bytes: ByteArray, encoding: String): String

    fun zlibInflate(input: ByteArray): ByteArray

    fun iconvDecode(input: ByteArray, encoding: String): String
    fun iconvEncode(input: String, encoding: String): ByteArray

    suspend fun setTimeout(delayMs: Long, callback: suspend () -> Unit): Long
    fun clearTimeout(id: Long)

    fun log(level: String, args: List<JsValue>)

    /** "android" | "jvm" | "ios" | "macos" */
    val platformTag: String
    val userAgent: String
}
