package top.iwesley.lyn.music.online.types

/**
 * 在线歌曲的全局标识。
 *
 * 由 `source`（源 id，如 `kw`/`kg`/`tx`/`wy`/`mg`/`xm`）与 `songmid` 组合唯一确定一首曲目；
 * 部分源在取 URL / 歌词时需要额外 `albumId`（如 wy），缺省为 `null`。
 *
 * [stableKey] 以 `"source:songmid"` 形式给出可用作 Map key / 缓存键的稳定字符串。
 *
 * 注：M0 不接序列化；若未来需要持久化传输，可在 M1 加 `@Serializable` 与相应 plugin。
 */
data class OnlineMusicId(
    val source: String,
    val songmid: String,
    val albumId: String? = null,
) {
    val stableKey: String
        get() = "$source:$songmid"
}
