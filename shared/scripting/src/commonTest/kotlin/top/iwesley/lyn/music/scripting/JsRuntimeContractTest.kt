package top.iwesley.lyn.music.scripting

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class JsRuntimeContractTest {

    private fun runtime(): JsRuntime =
        JsRuntimeFactory.create(sourceId = "test", bridge = StubBridge())

    @Test
    fun evaluate_numeric_expression_returns_num() = runTest {
        runtime().use { rt ->
            val r = rt.evaluate("1 + 2")
            assertEquals(JsValue.Num(3.0), r)
        }
    }

    @Test
    fun invoke_calls_nested_path() = runTest {
        runtime().use { rt ->
            rt.evaluate("var api = { foo: { bar: function(x){ return x * 2; } } };")
            val r = rt.invoke("api.foo.bar", JsValue.Num(5.0))
            assertEquals(JsValue.Num(10.0), r)
        }
    }

    @Test
    fun register_exposes_host_function_to_js() = runTest {
        runtime().use { rt ->
            rt.register("addOne") { args ->
                JsValue.Num((args[0] as JsValue.Num).value + 1)
            }
            val r = rt.evaluate("addOne(41)")
            assertEquals(JsValue.Num(42.0), r)
        }
    }
}

private class StubBridge : JsBridge {
    override suspend fun request(url: String, options: Map<String, JsValue>): JsValue = JsValue.Null
    override fun md5(input: ByteArray): String = "stub"
    override fun sha1(input: ByteArray): ByteArray = ByteArray(20)
    override fun sha256(input: ByteArray): ByteArray = ByteArray(32)
    override fun aesEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray?, mode: String) = data
    override fun desEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray?, mode: String) = data
    override fun rsaEncrypt(data: ByteArray, publicKeyPem: String) = data
    override fun base64Encode(input: ByteArray): String = ""
    override fun base64Decode(input: String): ByteArray = ByteArray(0)
    override fun bufferFrom(str: String, encoding: String): ByteArray = str.encodeToByteArray()
    override fun bufferToString(bytes: ByteArray, encoding: String): String = bytes.decodeToString()
    override fun zlibInflate(input: ByteArray): ByteArray = input
    override fun iconvDecode(input: ByteArray, encoding: String): String = input.decodeToString()
    override fun iconvEncode(input: String, encoding: String): ByteArray = input.encodeToByteArray()
    override suspend fun setTimeout(delayMs: Long, callback: suspend () -> Unit): Long = 0
    override fun clearTimeout(id: Long) {}
    override fun log(level: String, args: List<JsValue>) {}
    override val platformTag: String = "test"
    override val userAgent: String = "test-ua"
}
