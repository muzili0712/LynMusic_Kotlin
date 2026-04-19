package top.iwesley.lyn.music.scripting

import java.io.File
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * 冒烟：kw bundle 能被 GraalVM evaluate 不抛错。
 *
 * 只保证 JS 语法合法 + top-level 代码执行完成（挂到 globalThis.__lyn_source_kw）；
 * 具体 API 调用（search/url/lyric）由 T5/M5 的 Repository + 真 Bridge 才能验证。
 */
class KwAssetLoadTest {

    @Test
    fun kw_js_asset_evaluates_without_error() = runTest {
        val candidates = listOf(
            "../online/src/commonMain/composeResources/files/sdk/kw.js",
            "shared/online/src/commonMain/composeResources/files/sdk/kw.js",
            "../../shared/online/src/commonMain/composeResources/files/sdk/kw.js",
        )
        val file = candidates.map { File(it) }.firstOrNull { it.exists() }
            ?: error("kw.js bundle not found in any candidate path: $candidates (cwd=${File(".").absolutePath})")
        val js = file.readText()
        assertTrue(js.isNotBlank(), "kw.js must be non-empty")

        JsRuntimeFactory.create("kw", bridge = KwStubBridge()).use { rt ->
            // 注入最小 lyn 全局桥；真正桥接在 T5。
            rt.register("__lyn_md5") { args ->
                val input = when (val v = args[0]) {
                    is JsValue.Str -> v.value.encodeToByteArray()
                    is JsValue.Bytes -> v.data
                    else -> ByteArray(0)
                }
                JsValue.Str(KwStubBridge().md5(input))
            }
            // 最小 lyn 对象：top-level bundle 执行时可能不立刻调用，这里只要 evaluate 不抛即可。
            // GraalJS 默认没有 TextEncoder/TextDecoder，这里用纯 JS 的 UTF-8 编解码。
            rt.evaluate(
                """
                const utf8Encode = (s) => {
                    s = String(s);
                    const out = [];
                    for (let i = 0; i < s.length; i++) {
                        let c = s.charCodeAt(i);
                        if (c < 0x80) out.push(c);
                        else if (c < 0x800) { out.push(0xc0 | (c >> 6)); out.push(0x80 | (c & 0x3f)); }
                        else { out.push(0xe0 | (c >> 12)); out.push(0x80 | ((c >> 6) & 0x3f)); out.push(0x80 | (c & 0x3f)); }
                    }
                    return new Uint8Array(out);
                };
                const utf8Decode = (b) => {
                    let s = '';
                    const arr = b instanceof Uint8Array ? b : new Uint8Array(b || []);
                    for (let i = 0; i < arr.length; i++) s += String.fromCharCode(arr[i]);
                    return s;
                };
                globalThis.lyn = {
                    md5: (x) => __lyn_md5(typeof x === 'string' ? x : ''),
                    sha1: (x) => new Uint8Array(20),
                    sha256: (x) => new Uint8Array(32),
                    aesEncrypt: (d) => d,
                    desEncrypt: (d) => d,
                    rsaEncrypt: (d) => d,
                    base64Encode: (d) => '',
                    base64Decode: (s) => new Uint8Array(0),
                    bufferFrom: (s, _e) => utf8Encode(s),
                    bufferToString: (b, _e) => utf8Decode(b),
                    zlibInflate: (b) => b,
                    iconvDecode: (b, _e) => utf8Decode(b),
                    iconvEncode: (s, _e) => utf8Encode(s),
                    request: (_u, _o) => Promise.reject(new Error('stub')),
                    setTimeout: (_d, _cb) => 0,
                    clearTimeout: (_id) => {},
                    log: (_l, _a) => {},
                    platformTag: 'jvm',
                    userAgent: 'kw-asset-test-ua',
                };
                """.trimIndent(),
                "bootstrap-lyn.js"
            )

            rt.evaluate(js, "kw.js")
            // 产出检查：bundle 里 footer 挂到了 globalThis.__lyn_source_kw
            val probe = rt.evaluate(
                "typeof globalThis.__lyn_source_kw === 'object' && globalThis.__lyn_source_kw !== null",
                "probe.js"
            )
            assertTrue(
                probe is JsValue.Bool && probe.value,
                "expected globalThis.__lyn_source_kw to be a non-null object, got $probe"
            )
        }
    }
}

/** JsBridge 的最小 stub；M0 不需要真实现，evaluate 只会触发 top-level 代码。 */
private class KwStubBridge : JsBridge {
    override suspend fun request(url: String, options: Map<String, JsValue>): JsValue = JsValue.Null
    override fun md5(input: ByteArray): String = "stub"
    override fun sha1(input: ByteArray): ByteArray = ByteArray(20)
    override fun sha256(input: ByteArray): ByteArray = ByteArray(32)
    override fun aesEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray?, mode: String): ByteArray = data
    override fun desEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray?, mode: String): ByteArray = data
    override fun rsaEncrypt(data: ByteArray, publicKeyPem: String): ByteArray = data
    override fun base64Encode(input: ByteArray): String = ""
    override fun base64Decode(input: String): ByteArray = ByteArray(0)
    override fun bufferFrom(str: String, encoding: String): ByteArray = str.encodeToByteArray()
    override fun bufferToString(bytes: ByteArray, encoding: String): String = bytes.decodeToString()
    override fun zlibInflate(input: ByteArray): ByteArray = input
    override fun iconvDecode(input: ByteArray, encoding: String): String = input.decodeToString()
    override fun iconvEncode(input: String, encoding: String): ByteArray = input.encodeToByteArray()
    override suspend fun setTimeout(delayMs: Long, callback: suspend () -> Unit): Long = 0L
    override fun clearTimeout(id: Long) {}
    override fun log(level: String, args: List<JsValue>) {}
    override val platformTag: String = "jvm"
    override val userAgent: String = "kw-asset-test-ua"
}
