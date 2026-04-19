package top.iwesley.lyn.music.online

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import top.iwesley.lyn.music.scripting.HostFunction
import top.iwesley.lyn.music.scripting.JsRuntime
import top.iwesley.lyn.music.scripting.JsValue

/**
 * 轻量 [JsRuntime] stub，测试中替代 [top.iwesley.lyn.music.scripting.JsRuntimeFactory] 真实现。
 *
 * - [evaluate] 直接吞掉脚本返回 [JsValue.Undefined]（bundle bootstrap 依赖 real runtime，测试无关心）。
 * - [invoke] 委派给构造时传入的 handler；handler 可根据 path / args 构造假返回。
 * - [register] 记录名字但不实际注册，用于断言"桥函数已注册某些名字"。
 */
class FakeJsRuntime(
    override val sourceId: String,
    private val invokeHandler: suspend (path: String, args: List<JsValue>) -> JsValue,
) : JsRuntime {

    override val dispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    val registeredNames: MutableList<String> = mutableListOf()
    var lastInvokePath: String? = null
        private set
    var lastInvokeArgs: List<JsValue> = emptyList()
        private set

    override suspend fun evaluate(script: String, name: String): JsValue = JsValue.Undefined

    override suspend fun invoke(path: String, vararg args: JsValue): JsValue {
        lastInvokePath = path
        lastInvokeArgs = args.toList()
        return invokeHandler(path, args.toList())
    }

    override fun register(name: String, host: HostFunction) {
        registeredNames += name
    }

    override fun close() = Unit
}
