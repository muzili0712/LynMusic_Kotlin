package top.iwesley.lyn.music.online.resolve

import top.iwesley.lyn.music.online.types.PlayableUrl
import top.iwesley.lyn.music.online.types.Quality

/**
 * 单源 URL 拿取器。每个启用源（kw/kg/tx/wy/mg）实现一个，返回真实可播放 URL。
 *
 * 替代 M0 的 "JS 引擎调 api-source stub" 路径（M0 T3 的 INTERNAL_SHIMS.lx-internal-api-source
 * 只返回 throw stub）。M1.0 把 getMusicUrl 从 JS 层升到 Kotlin 层，search/lyric/pic 仍走 JS。
 */
interface SourceUrlResolver {
    suspend fun resolve(songmid: String, quality: Quality): PlayableUrl
}
