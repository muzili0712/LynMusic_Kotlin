package top.iwesley.lyn.music.scripting

/**
 * 统一的 JS 返回值表示 — 跨平台（QuickJS / GraalVM / JavaScriptCore）解包到同一 sealed class。
 * 每个 actual runtime 负责把原生 JS 值往返转换到 [JsValue]。
 */
sealed class JsValue {
    data object Null : JsValue()
    data object Undefined : JsValue()
    data class Bool(val value: Boolean) : JsValue()
    data class Num(val value: Double) : JsValue()
    data class Str(val value: String) : JsValue()
    data class Arr(val items: List<JsValue>) : JsValue()
    data class Obj(val entries: Map<String, JsValue>) : JsValue()
    data class Bytes(val data: ByteArray) : JsValue() {
        override fun equals(other: Any?): Boolean =
            other is Bytes && data.contentEquals(other.data)

        override fun hashCode(): Int = data.contentHashCode()
    }
}

fun JsValue.asStringOrNull(): String? = (this as? JsValue.Str)?.value
fun JsValue.asObjOrNull(): Map<String, JsValue>? = (this as? JsValue.Obj)?.entries
