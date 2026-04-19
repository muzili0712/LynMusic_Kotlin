package top.iwesley.lyn.music.scripting

import kotlinx.coroutines.CoroutineDispatcher

/**
 * 单 source 一个实例的 JS 运行时门面。所有跨线程安全细节（例如 GraalVM Context 绑定单线程、
 * QuickJS `checkSameThread`）由 actual 内部处理，上层只关心 suspend API。
 */
interface JsRuntime : AutoCloseable {
    suspend fun evaluate(script: String, name: String = "anon.js"): JsValue
    suspend fun invoke(path: String, vararg args: JsValue): JsValue

    /**
     * 注册 host 函数到 JS 全局作用域。
     *
     * 注意：actual 实现内部使用 runBlocking 同步切换到 runtime 专用 dispatcher；
     * 不要从该 dispatcher 线程上的协程调用 register，否则死锁。通常 register 只在
     * JsRuntime 创建后、evaluate 前一次性完成。
     */
    fun register(name: String, host: HostFunction)
    val dispatcher: CoroutineDispatcher
    val sourceId: String
}

expect object JsRuntimeFactory {
    fun create(sourceId: String, bridge: JsBridge): JsRuntime
}
