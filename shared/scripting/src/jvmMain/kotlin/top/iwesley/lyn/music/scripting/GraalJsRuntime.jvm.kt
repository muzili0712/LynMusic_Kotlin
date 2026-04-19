package top.iwesley.lyn.music.scripting

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Source
import org.graalvm.polyglot.Value
import org.graalvm.polyglot.proxy.ProxyExecutable
import java.util.concurrent.Executors

actual object JsRuntimeFactory {
    actual fun create(sourceId: String, bridge: JsBridge): JsRuntime =
        GraalJsRuntime(sourceId, bridge)
}

/**
 * GraalVM polyglot-based [JsRuntime] actual.
 *
 * GraalVM `Context` 不是线程安全的——同一个 Context 必须始终在创建它的线程上
 * `enter/leave`，否则抛 `PolyglotException: context is not thread-safe`。
 * 做法：所有 JS 操作统一 `withContext(dispatcher)`，dispatcher 背靠一个单线程 executor。
 */
private class GraalJsRuntime(
    override val sourceId: String,
    @Suppress("unused") private val bridge: JsBridge,
) : JsRuntime {
    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "js-$sourceId").apply { isDaemon = true }
    }
    override val dispatcher: CoroutineDispatcher = executor.asCoroutineDispatcher()

    // Context build 本身可在任意线程；后续 eval 才要求绑定单线程
    private val context: Context = Context.newBuilder("js")
        .allowHostAccess(HostAccess.ALL)
        .build()

    init {
        // 在 executor 线程上 enter/leave 一次做 bootstrap，保证后续该线程一直持有 context
        runBlocking(dispatcher) {
            withContext0 {
                context.eval("js", "globalThis.lyn = globalThis.lyn || {};")
            }
        }
    }

    override suspend fun evaluate(script: String, name: String): JsValue =
        withContext(dispatcher) {
            withContext0 {
                try {
                    val src = Source.newBuilder("js", script, name).build()
                    context.eval(src).toJsValue()
                } catch (t: Throwable) {
                    if (t is MusicSourceException) throw t
                    throw MusicSourceException.ScriptRuntimeError(sourceId, t.message ?: "", t)
                }
            }
        }

    override suspend fun invoke(path: String, vararg args: JsValue): JsValue =
        withContext(dispatcher) {
            withContext0 {
                try {
                    var cur: Value = context.getBindings("js")
                    for (seg in path.split('.')) {
                        cur = cur.getMember(seg)
                            ?: throw MusicSourceException.ScriptRuntimeError(
                                sourceId, "path not found: $path", null,
                            )
                    }
                    val result = cur.execute(*args.map { it.toHost() }.toTypedArray())
                    result.toJsValue()
                } catch (t: Throwable) {
                    if (t is MusicSourceException) throw t
                    throw MusicSourceException.ScriptRuntimeError(sourceId, t.message ?: "", t)
                }
            }
        }

    override fun register(name: String, host: HostFunction) {
        runBlocking(dispatcher) {
            withContext0 {
                // TODO(T5 死锁风险): 若 host(...) 内部 withContext(dispatcher) 试图切回本单线程 dispatcher，
                // runBlocking 会死锁。T5 真正 IO/网络 HostFunction 落地前，必须评估：
                //   a) 换 ProxyPromise 异步桥（推荐）
                //   b) host 约定不得 withContext(同 dispatcher)
                //   c) HostFunction 改为非 suspend，IO 提前在调用方完成
                // 当前 M0 bridge 全部同步 compute-only，runBlocking 安全。
                val proxy = ProxyExecutable { graalArgs ->
                    val parsed = graalArgs.map { it.toJsValue() }
                    runBlocking { host(parsed).toHost() }
                }
                context.getBindings("js").putMember(name, proxy)
            }
        }
    }

    override fun close() {
        try {
            runBlocking(dispatcher) {
                withContext0 { /* ensure we're on the bound thread */ }
                try {
                    context.close(true)
                } catch (_: Throwable) {
                }
            }
        } catch (_: Throwable) {
        }
        executor.shutdownNow()
    }

    private inline fun <T> withContext0(block: () -> T): T {
        context.enter()
        try {
            return block()
        } finally {
            context.leave()
        }
    }
}

private fun Value.toJsValue(): JsValue = when {
    isNull -> JsValue.Null
    isBoolean -> JsValue.Bool(asBoolean())
    isNumber -> JsValue.Num(asDouble())
    isString -> JsValue.Str(asString())
    hasArrayElements() -> JsValue.Arr(
        (0 until arraySize).map { getArrayElement(it).toJsValue() },
    )
    hasMembers() -> JsValue.Obj(
        memberKeys.associateWith { k -> getMember(k).toJsValue() },
    )
    else -> JsValue.Undefined
}

private fun JsValue.toHost(): Any? = when (this) {
    is JsValue.Null -> null
    is JsValue.Undefined -> null
    is JsValue.Bool -> value
    is JsValue.Num -> value
    is JsValue.Str -> value
    is JsValue.Bytes -> data
    is JsValue.Arr -> items.map { it.toHost() }
    is JsValue.Obj -> entries.mapValues { (_, v) -> v.toHost() }
}
