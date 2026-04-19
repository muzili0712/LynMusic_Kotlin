package top.iwesley.lyn.music.online.types

/**
 * 在线搜索 / 榜单 / 歌单等结果中返回的单首歌曲。
 *
 * - [id] 唯一标识（见 [OnlineMusicId]）。
 * - [name] / [singer] 展示字段。
 * - [album] / [albumId] 专辑名与专辑 id（可选）。
 * - [intervalSeconds] 时长（秒），未知为 0。
 * - [coverUrl] 封面 URL；可能过期，UI 层自行决定缓存策略。
 * - [availableQualities] 源声明可用音质列表；空集合表示未知，调用方应走降级顺序探测。
 * - [defaultQuality] 该源/该曲的默认首选音质，若 null 则由上层从 [availableQualities] 或设置中心决定。
 */
data class OnlineSong(
    val id: OnlineMusicId,
    val name: String,
    val singer: String,
    val album: String? = null,
    val albumId: String? = null,
    val intervalSeconds: Int = 0,
    val coverUrl: String? = null,
    val availableQualities: List<Quality> = emptyList(),
    val defaultQuality: Quality? = null,
)
