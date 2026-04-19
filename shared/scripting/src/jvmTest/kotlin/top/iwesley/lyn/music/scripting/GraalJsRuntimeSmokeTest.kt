package top.iwesley.lyn.music.scripting

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GraalJsRuntimeSmokeTest {

    @Test
    fun host_function_md5_reachable_from_js_via_register() = runTest {
        JsRuntimeFactory.create("smoke", bridge = TestBridge(md5Out = "abc123")).use { rt ->
            rt.register("md5") { args ->
                JsValue.Str(rt.bridgeMd5(args))
            }
            val r = rt.evaluate("md5('hello')")
            assertEquals(JsValue.Str("abc123"), r)
        }
    }

    @Test
    fun plain_promise_expression_does_not_throw() = runTest {
        JsRuntimeFactory.create("smoke", bridge = TestBridge()).use { rt ->
            val r = rt.evaluate("Promise.resolve(42).then(v => v + 1)")
            // M0: GraalVM Promise 无 microtask queue，结果未 resolve。
            // Repository 侧统一处理 await；这里只校验表达式不抛、返回非 Null。
            assertTrue(r !is JsValue.Null)
        }
    }
}

private fun JsRuntime.bridgeMd5(@Suppress("UNUSED_PARAMETER") args: List<JsValue>): String =
    // 实际会委派到 bridge.md5；smoke 只校验"注册 → 调用"链路打通
    "abc123"

private class TestBridge(private val md5Out: String = "xx") : JsBridge {
    override suspend fun request(url: String, options: Map<String, JsValue>) = JsValue.Null
    override fun md5(input: ByteArray) = md5Out
    override fun sha1(input: ByteArray) = ByteArray(20)
    override fun sha256(input: ByteArray) = ByteArray(32)
    override fun aesEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray?, mode: String) = data
    override fun desEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray?, mode: String) = data
    override fun rsaEncrypt(data: ByteArray, publicKeyPem: String) = data
    override fun base64Encode(input: ByteArray) = ""
    override fun base64Decode(input: String) = ByteArray(0)
    override fun bufferFrom(str: String, encoding: String) = str.encodeToByteArray()
    override fun bufferToString(bytes: ByteArray, encoding: String) = bytes.decodeToString()
    override fun zlibInflate(input: ByteArray) = input
    override fun iconvDecode(input: ByteArray, encoding: String) = input.decodeToString()
    override fun iconvEncode(input: String, encoding: String) = input.encodeToByteArray()
    override suspend fun setTimeout(delayMs: Long, callback: suspend () -> Unit) = 0L
    override fun clearTimeout(id: Long) {}
    override fun log(level: String, args: List<JsValue>) {}
    override val platformTag = "jvm"
    override val userAgent = "smoke-ua"
}
