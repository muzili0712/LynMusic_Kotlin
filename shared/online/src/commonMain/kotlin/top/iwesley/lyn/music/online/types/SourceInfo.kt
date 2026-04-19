package top.iwesley.lyn.music.online.types

/**
 * 源声明的能力域。与 lx-music-mobile SDK 的 `actions` 约定保持一致。
 */
enum class SourceMethod {
    Search,
    Url,
    Lyric,
    Pic,
    Leaderboard,
    Songlist,
    HotSearch,
    TipSearch,
    Comment,
}

/**
 * 单个音乐源的元信息。
 *
 * - [id] 源 id（如 `kw`/`kg`/`tx`/`wy`/`mg`/`xm`）。
 * - [displayName] 展示名。
 * - [supportedQualities] 源默认可提供的音质集（可能被单曲级 `availableQualities` 覆盖）。
 * - [methods] 源声明支持的能力域集合。
 * - [enabled] M0 是否启用；关闭源会在 UI 列表中隐藏（或灰化+原因）。
 * - [disabledReason] 当 [enabled] 为 false 时，给出对外解释（便于排查/反馈）。
 */
data class SourceInfo(
    val id: String,
    val displayName: String,
    val supportedQualities: Set<Quality>,
    val methods: Set<SourceMethod>,
    val enabled: Boolean,
    val disabledReason: String? = null,
)
