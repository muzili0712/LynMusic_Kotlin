package top.iwesley.lyn.music.scripting

import com.whl.quickjs.wrapper.JSArray
import com.whl.quickjs.wrapper.JSCallFunction
import com.whl.quickjs.wrapper.JSFunction
import com.whl.quickjs.wrapper.JSObject
import com.whl.quickjs.wrapper.QuickJSContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

actual object JsRuntimeFactory {
    actual fun create(sourceId: String, bridge: JsBridge): JsRuntime =
        QuickJsRuntime(sourceId, bridge)
}

/**
 * Android actual based on `wang.harlon.quickjs:wrapper-android` (fka "WeChat QuickJS wrapper",
 * now maintained by HarlonWang). API surface inspected from artifact 3.2.0:
 *
 *  - [QuickJSContext.create] / [QuickJSContext.evaluate] / [QuickJSContext.getGlobalObject]
 *    / [QuickJSContext.destroy]
 *  - [JSObject.setProperty]`(String, JSCallFunction)` for host function injection
 *  - [JSFunction.call]`(vararg Any)` for js function invocation
 *  - Context enforces single-thread access via internal `checkSameThread()`; we run everything
 *    on a dedicated executor so `runBlocking(dispatcher)` is safe from the constructor line.
 */
private class QuickJsRuntime(
    override val sourceId: String,
    @Suppress("unused") private val bridge: JsBridge,
) : JsRuntime {
    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "quickjs-$sourceId").apply { isDaemon = true }
    }
    override val dispatcher: CoroutineDispatcher = executor.asCoroutineDispatcher()

    private val ctx: QuickJSContext = runBlocking(dispatcher) {
        QuickJSContext.create().also {
            it.evaluate("globalThis.lyn = globalThis.lyn || {};", "bootstrap.js")
        }
    }

    override suspend fun evaluate(script: String, name: String): JsValue =
        withContext(dispatcher) {
            try {
                ctx.evaluate(script, name).toJsValue()
            } catch (t: Throwable) {
                if (t is MusicSourceException) throw t
                throw MusicSourceException.ScriptRuntimeError(sourceId, t.message ?: "", t)
            }
        }

    override suspend fun invoke(path: String, vararg args: JsValue): JsValue =
        withContext(dispatcher) {
            try {
                var cur: Any? = ctx.globalObject
                for (seg in path.split('.')) {
                    cur = (cur as? JSObject)?.getProperty(seg)
                        ?: throw MusicSourceException.ScriptRuntimeError(
                            sourceId, "path not found: $path", null,
                        )
                }
                val fn = cur as? JSFunction
                    ?: throw MusicSourceException.ScriptRuntimeError(
                        sourceId, "not a function: $path", null,
                    )
                fn.call(*args.map { it.toHost() }.toTypedArray()).toJsValue()
            } catch (t: Throwable) {
                if (t is MusicSourceException) throw t
                throw MusicSourceException.ScriptRuntimeError(sourceId, t.message ?: "", t)
            }
        }

    override fun register(name: String, host: HostFunction) {
        runBlocking(dispatcher) {
            val callback = JSCallFunction { args ->
                // host 是 suspend；同 GraalVM 路径，M0 由 bridge stub 同步返回，
                // runBlocking 不会死锁。T5 若宿主函数真做 IO，需评估换异步 promise 桥。
                runBlocking {
                    host(args.map { it.toJsValue() }).toHost()
                }
            }
            ctx.globalObject.setProperty(name, callback)
        }
    }

    override fun close() {
        try {
            runBlocking(dispatcher) {
                try {
                    ctx.destroy()
                } catch (_: Throwable) {
                }
            }
        } catch (_: Throwable) {
        }
        executor.shutdownNow()
    }
}

private fun Any?.toJsValue(): JsValue = when (this) {
    null -> JsValue.Null
    is Boolean -> JsValue.Bool(this)
    is Int -> JsValue.Num(this.toDouble())
    is Long -> JsValue.Num(this.toDouble())
    is Double -> JsValue.Num(this)
    is Float -> JsValue.Num(this.toDouble())
    is String -> JsValue.Str(this)
    is ByteArray -> JsValue.Bytes(this)
    is JSArray -> {
        val len = this.length()
        JsValue.Arr((0 until len).map { i -> this.get(i).toJsValue() })
    }
    is JSObject -> {
        // 走 getNames → JSArray(String)，再逐字段取回
        val keys = this.names
        val size = keys.length()
        val map = LinkedHashMap<String, JsValue>(size)
        for (i in 0 until size) {
            val k = keys.get(i) as? String ?: continue
            map[k] = this.getProperty(k).toJsValue()
        }
        JsValue.Obj(map)
    }
    else -> JsValue.Undefined
}

private fun JsValue.toHost(): Any? = when (this) {
    is JsValue.Null, JsValue.Undefined -> null
    is JsValue.Bool -> value
    is JsValue.Num -> value
    is JsValue.Str -> value
    is JsValue.Bytes -> data
    is JsValue.Arr -> items.map { it.toHost() }.toTypedArray()
    is JsValue.Obj -> entries.mapValues { (_, v) -> v.toHost() }
}
