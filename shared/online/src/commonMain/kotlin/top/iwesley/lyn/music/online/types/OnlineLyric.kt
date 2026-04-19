package top.iwesley.lyn.music.online.types

/**
 * 在线歌词三（四）件套。
 *
 * - [original] 原文歌词（LRC 文本，必选）。
 * - [translation] 翻译（通常中文），可选。
 * - [romanization] 罗马音（日/韩常见），可选。
 * - [enhanced] 逐字 / 卡拉 OK 增强歌词（LX 在增强模式下返回的扩展 LRC），可选。
 *
 * 注：字段保留 lx-music-mobile 原生约定命名，避免在桥接层再做映射。
 */
data class OnlineLyric(
    val original: String,
    val translation: String? = null,
    val romanization: String? = null,
    val enhanced: String? = null,
)
