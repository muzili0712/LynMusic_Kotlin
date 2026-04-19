package top.iwesley.lyn.music.online.types

/**
 * M0 在线音乐源清单。
 *
 * 启用：kw / kg / tx / wy / mg（五源全量：Search/Url/Lyric/Pic 必备；M0 不在清单层做能力裁剪，
 * 运行时失败由 JS 桥 → Result 错误统一返回）。
 * 禁用：xm（lx-music-mobile 官方本身即为 stub，保留占位以便 UI 展示"暂不可用"而不是消失）。
 *
 * 注：音质集合 [Quality.degradationOrder] 仅作上限参考，实际可用音质以单曲级 `availableQualities` 为准。
 */
object SourceManifest {

    /** 九项能力全集。启用源默认声明支持全部能力，运行时失败由错误通道返回。 */
    private val allMethods: Set<SourceMethod> = SourceMethod.entries.toSet()

    val all: List<SourceInfo> = listOf(
        SourceInfo(
            id = "kw",
            displayName = "酷我音乐",
            supportedQualities = setOf(
                Quality.FLAC,
                Quality.K320,
                Quality.K192,
                Quality.K128,
            ),
            methods = allMethods,
            enabled = true,
        ),
        SourceInfo(
            id = "kg",
            displayName = "酷狗音乐",
            supportedQualities = setOf(
                Quality.FLAC24BIT,
                Quality.FLAC,
                Quality.K320,
                Quality.K128,
            ),
            methods = allMethods,
            enabled = true,
        ),
        SourceInfo(
            id = "tx",
            displayName = "QQ 音乐",
            supportedQualities = setOf(
                Quality.FLAC24BIT,
                Quality.FLAC,
                Quality.K320,
                Quality.K128,
            ),
            methods = allMethods,
            enabled = true,
        ),
        SourceInfo(
            id = "wy",
            displayName = "网易云音乐",
            supportedQualities = setOf(
                Quality.FLAC,
                Quality.K320,
                Quality.K192,
                Quality.K128,
            ),
            methods = allMethods,
            enabled = true,
        ),
        SourceInfo(
            id = "mg",
            displayName = "咪咕音乐",
            supportedQualities = setOf(
                Quality.FLAC24BIT,
                Quality.FLAC,
                Quality.K320,
                Quality.K128,
            ),
            methods = allMethods,
            enabled = true,
        ),
        SourceInfo(
            id = "xm",
            displayName = "虾米（暂不可用）",
            supportedQualities = emptySet(),
            methods = emptySet(),
            enabled = false,
            disabledReason = "lx-music-mobile 官方 xm 源为 stub，未提供可用实现，M0 暂不启用。",
        ),
    )

    /** M0 实际启用的源（UI 列表/搜索聚合的默认范围）。 */
    val enabled: List<SourceInfo>
        get() = all.filter { it.enabled }

    /** 按源 id 精确查找；未命中返回 null。 */
    fun byId(id: String): SourceInfo? = all.firstOrNull { it.id == id }
}
