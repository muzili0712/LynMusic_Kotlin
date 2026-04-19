package top.iwesley.lyn.music.online.types

/**
 * M0 在线音乐源清单。
 *
 * 启用：kw / kg / tx / wy / mg（五源全量：Search/Url/Lyric/Pic 必备）。
 * 禁用：xm（lx-music-mobile 官方本身即为 stub，保留占位以便 UI 展示"暂不可用"而不是消失）。
 *
 * 注：音质集合 [Quality.degradationOrder] 仅作上限参考，实际可用音质以单曲级 `availableQualities` 为准。
 */
object SourceManifest {
    val all: List<SourceInfo> = listOf(
        SourceInfo(
            id = "kw",
            name = "酷我音乐",
            enabled = true,
            methods = setOf(
                SourceMethod.Search,
                SourceMethod.Url,
                SourceMethod.Lyric,
                SourceMethod.Pic,
                SourceMethod.Leaderboard,
                SourceMethod.Songlist,
                SourceMethod.HotSearch,
                SourceMethod.TipSearch,
            ),
            qualities = listOf(
                Quality.FLAC,
                Quality.K320,
                Quality.K192,
                Quality.K128,
            ),
        ),
        SourceInfo(
            id = "kg",
            name = "酷狗音乐",
            enabled = true,
            methods = setOf(
                SourceMethod.Search,
                SourceMethod.Url,
                SourceMethod.Lyric,
                SourceMethod.Pic,
                SourceMethod.Leaderboard,
                SourceMethod.Songlist,
                SourceMethod.HotSearch,
                SourceMethod.TipSearch,
            ),
            qualities = listOf(
                Quality.FLAC24BIT,
                Quality.FLAC,
                Quality.K320,
                Quality.K128,
            ),
        ),
        SourceInfo(
            id = "tx",
            name = "QQ 音乐",
            enabled = true,
            methods = setOf(
                SourceMethod.Search,
                SourceMethod.Url,
                SourceMethod.Lyric,
                SourceMethod.Pic,
                SourceMethod.Leaderboard,
                SourceMethod.Songlist,
                SourceMethod.HotSearch,
                SourceMethod.TipSearch,
                SourceMethod.Comment,
            ),
            qualities = listOf(
                Quality.FLAC24BIT,
                Quality.FLAC,
                Quality.K320,
                Quality.K128,
            ),
        ),
        SourceInfo(
            id = "wy",
            name = "网易云音乐",
            enabled = true,
            methods = setOf(
                SourceMethod.Search,
                SourceMethod.Url,
                SourceMethod.Lyric,
                SourceMethod.Pic,
                SourceMethod.Leaderboard,
                SourceMethod.Songlist,
                SourceMethod.HotSearch,
                SourceMethod.TipSearch,
                SourceMethod.Comment,
            ),
            qualities = listOf(
                Quality.FLAC24BIT,
                Quality.FLAC,
                Quality.K320,
                Quality.K128,
            ),
        ),
        SourceInfo(
            id = "mg",
            name = "咪咕音乐",
            enabled = true,
            methods = setOf(
                SourceMethod.Search,
                SourceMethod.Url,
                SourceMethod.Lyric,
                SourceMethod.Pic,
                SourceMethod.Leaderboard,
                SourceMethod.Songlist,
            ),
            qualities = listOf(
                Quality.FLAC24BIT,
                Quality.FLAC,
                Quality.K320,
                Quality.K128,
            ),
        ),
        SourceInfo(
            id = "xm",
            name = "虾米音乐",
            enabled = false,
            disabledReason = "lx-music-mobile 官方 xm 源为 stub，未提供可用实现，M0 暂不启用。",
            methods = emptySet(),
            qualities = emptyList(),
        ),
    )

    /** M0 实际启用的源（UI 列表/搜索聚合的默认范围）。 */
    val enabled: List<SourceInfo>
        get() = all.filter { it.enabled }

    /** 按源 id 精确查找；未命中返回 null。 */
    fun byId(id: String): SourceInfo? = all.firstOrNull { it.id == id }
}
