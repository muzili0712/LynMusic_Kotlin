package top.iwesley.lyn.music.online.diagnostics

/**
 * 在线音乐域下的诊断日志 tag 集合。跟 [top.iwesley.lyn.music.core.model.GlobalDiagnosticLogger] 配合使用。
 *
 * - [source] 按源 id 生成 "MSRC.<id>"（例如 "MSRC.kw"），方便按源过滤。
 * - [RESOLVER] / [CACHE] / [BRIDGE] 常量覆盖 Repository / 缓存 / JS 桥三个水平切面。
 */
object OnlineLogTags {
    fun source(id: String): String = "MSRC.$id"
    const val RESOLVER: String = "MSRC.resolver"
    const val CACHE: String = "MSRC.cache"
    const val BRIDGE: String = "MSRC.bridge"
}
