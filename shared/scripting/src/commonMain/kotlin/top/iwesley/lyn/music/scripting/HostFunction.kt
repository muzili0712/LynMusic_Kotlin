package top.iwesley.lyn.music.scripting

/**
 * 从 JS 调用的宿主函数签名。JsBridge 把各原生能力（crypto/http/...）
 * 以这种 suspend lambda 注册进 [JsRuntime.register]。
 */
typealias HostFunction = suspend (args: List<JsValue>) -> JsValue
