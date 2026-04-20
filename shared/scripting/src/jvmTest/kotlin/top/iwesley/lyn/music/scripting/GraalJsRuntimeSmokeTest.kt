package top.iwesley.lyn.music.scripting

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GraalJsRuntimeSmokeTest {

    @Test
    fun host_function_md5_reachable_from_js_via_register() = runTest {
        val bridge = TestBridge(md5Out = "abc123")
        JsRuntimeFactory.create("smoke", bridge = bridge).use { rt ->
            rt.register("md5") { args ->
                val input = (args[0] as JsValue.Str).value.encodeToByteArray()
                JsValue.Str(bridge.md5(input))
            }
            val r = rt.evaluate("md5('hello')")
            assertEquals(JsValue.Str("abc123"), r)
        }
    }

    @Test
    fun plain_promise_resolves() = runTest {
        JsRuntimeFactory.create("smoke", bridge = TestBridge()).use { rt ->
            val r = rt.evaluate("Promise.resolve(42).then(v => v + 1)")
            // GraalVM 无 await 语义，Promise 反射为 JsValue.Obj；
            // M0 仅验证"表达式执行不抛 + 反射为对象"，真正 await 在 Repository 层处理
            assertTrue(r is JsValue.Obj, "expected Promise reflected as Obj, got $r")
        }
    }
}

private class TestBridge(private val md5Out: String = "xx") : JsBridge {
    override suspend fun request(url: String, options: Map<String, JsValue>) = JsValue.Null
    override fun md5(input: ByteArray) = md5Out
    override fun sha1(input: ByteArray) = ByteArray(20)
    override fun sha256(input: ByteArray) = ByteArray(32)
    override fun aesEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray?, mode: String) = data
    override fun desEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray?, mode: String) = data
    override fun rsaEncrypt(data: ByteArray, publicKeyPem: String, padding: String) = data
    override fun base64Encode(input: ByteArray) = ""
    override fun base64Decode(input: String) = ByteArray(0)
    override fun bufferFrom(str: String, encoding: String) = str.encodeToByteArray()
    override fun bufferToString(bytes: ByteArray, encoding: String) = bytes.decodeToString()
    override fun zlibInflate(input: ByteArray, format: String) = input
    override fun iconvDecode(input: ByteArray, encoding: String) = input.decodeToString()
    override fun iconvEncode(input: String, encoding: String) = input.encodeToByteArray()
    override suspend fun setTimeout(delayMs: Long, callback: suspend () -> Unit) = 0L
    override fun clearTimeout(id: Long) {}
    override fun log(level: String, args: List<JsValue>) {}
    override val platformTag = "jvm"
    override val userAgent = "smoke-ua"
}
