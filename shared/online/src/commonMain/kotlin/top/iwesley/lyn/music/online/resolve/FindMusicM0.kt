package top.iwesley.lyn.music.online.resolve

import top.iwesley.lyn.music.online.repository.OnlineMusicRepository
import top.iwesley.lyn.music.online.types.OnlineSong
import top.iwesley.lyn.music.online.types.SourceManifest

/**
 * 跨源查找替代歌曲（M0 简化版）。
 *
 * 两趟（2-pass）匹配策略：
 * - **Pass 1**：`name` 与 `singer` 精确一致（归一化后），且时长差 ≤ [durationToleranceSec] 秒。
 * - **Pass 2**：`name` 精确一致 + `singer` 至少有一位主歌手重合（按 `, 、 / &` 拆分后取交集）。
 *
 * 归一化规则：trim → 转小写 → 去空格与圆括号/方括号（含中英文）。
 * 不启用模糊匹配（levenshtein 等）以避免错配；M1 会引入基于 token 的相似度 + duration 打分。
 *
 * 错误容忍：某源搜索失败（抛异常）时整源跳过，不影响下一个源。
 */
class FindMusicM0(
    private val repository: OnlineMusicRepository,
    private val durationToleranceSec: Int = 3,
) {
    /**
     * 在除 [excludeSource] 外的所有启用源中寻找与 [target] 匹配的歌曲。
     *
     * @return 第一个命中（Pass 1 优先于 Pass 2，按 [SourceManifest.enabled] 顺序逐源扫描）；全部未命中返回 `null`。
     */
    suspend fun find(target: OnlineSong, excludeSource: String): OnlineSong? {
        val targetName = normalize(target.name)
        val targetSinger = normalize(target.singer)
        val keyword = "${target.name} ${target.singer}".trim()
        val targetSingers = splitSingers(targetSinger)

        for (src in SourceManifest.enabled) {
            if (src.id == excludeSource) continue
            val page = runCatching {
                repository.search(src.id, keyword, page = 1, limit = 20)
            }.getOrNull() ?: continue

            // Pass 1: 严格 —— name + singer + duration（容差 [durationToleranceSec]）
            page.items.firstOrNull { candidate ->
                normalize(candidate.name) == targetName &&
                    normalize(candidate.singer) == targetSinger &&
                    kotlin.math.abs(candidate.intervalSeconds - target.intervalSeconds) <= durationToleranceSec
            }?.let { return it }

            // Pass 2: 宽松 —— name 精确 + 主歌手任一重合
            page.items.firstOrNull { candidate ->
                normalize(candidate.name) == targetName &&
                    splitSingers(normalize(candidate.singer)).any { it in targetSingers }
            }?.let { return it }
        }
        return null
    }

    private fun splitSingers(normalized: String): Set<String> =
        normalized.split(Regex("[,、/&]"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()

    private fun normalize(s: String): String =
        s.trim().lowercase().replace(Regex("[\\s()（）\\[\\]【】]"), "")
}
