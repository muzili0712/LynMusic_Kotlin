package top.iwesley.lyn.music.online.types

/**
 * 在线搜索 / 榜单 / 歌单等结果中返回的单首歌曲。
 *
 * - [id] 唯一标识（见 [OnlineMusicId]）。
 * - [name] / [singer] 展示字段。
 * - [album] / [albumId] 专辑名与专辑 id（可空）。
 * - [intervalSeconds] 时长（秒），未知为 0。
 * - [coverUrl] 封面 URL（可空）；可能过期，UI 层自行决定缓存策略。
 * - [availableQualities] 源声明可用音质列表；空列表表示未知，调用方应走降级顺序探测。
 * - [defaultQuality] 该源/该曲的默认首选音质（非空）。
 */
data class OnlineSong(
    val id: OnlineMusicId,
    val name: String,
    val singer: String,
    val album: String?,
    val albumId: String?,
    val intervalSeconds: Int,
    val coverUrl: String?,
    val availableQualities: List<Quality>,
    val defaultQuality: Quality,
)
