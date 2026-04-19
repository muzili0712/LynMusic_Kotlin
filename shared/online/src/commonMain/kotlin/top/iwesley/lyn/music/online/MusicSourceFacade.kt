package top.iwesley.lyn.music.online

import top.iwesley.lyn.music.online.types.OnlineLyric
import top.iwesley.lyn.music.online.types.OnlineMusicId
import top.iwesley.lyn.music.online.types.OnlineSong
import top.iwesley.lyn.music.online.types.PlayableUrl
import top.iwesley.lyn.music.online.types.Quality
import top.iwesley.lyn.music.online.types.SearchPage
import top.iwesley.lyn.music.online.types.SourceInfo

/**
 * M0 在线音乐源聚合门面。仅暴露 Search / Url / Lyric / Pic 四种能力；
 * 上层 Repository（T6）按 [sources] 做源路由 + 聚合，UI（T7）不直接接触此接口。
 *
 * 实现由 [DefaultMusicSourceFacade] 持有 kw/kg/tx/wy/mg 五个 [top.iwesley.lyn.music.online.source.JsMusicSource] 实例；
 * 未启用（如 xm）的源调用会抛 [top.iwesley.lyn.music.scripting.MusicSourceException.SourceDisabled]。
 */
interface MusicSourceFacade {
    val sources: List<SourceInfo>
    suspend fun search(sourceId: String, keyword: String, page: Int = 1, limit: Int = 30): SearchPage<OnlineSong>
    suspend fun getPlayableUrl(id: OnlineMusicId, quality: Quality): PlayableUrl
    suspend fun getLyric(id: OnlineMusicId): OnlineLyric
    suspend fun getPic(id: OnlineMusicId): String
}
