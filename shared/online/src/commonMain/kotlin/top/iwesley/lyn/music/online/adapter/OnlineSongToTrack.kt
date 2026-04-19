package top.iwesley.lyn.music.online.adapter

import top.iwesley.lyn.music.core.model.Track
import top.iwesley.lyn.music.online.types.OnlineMusicId
import top.iwesley.lyn.music.online.types.OnlineSong
import top.iwesley.lyn.music.online.types.Quality

/**
 * T10: `OnlineSong` ↔ `Track` 适配器。
 *
 * M0 采取 "lazy URI" 策略：Track 的 [Track.mediaLocator] 承载 `online-lazy://<source>/<songmid>?q=<quality>`
 * 形式的占位 URI；真正的 CDN URL 由 T9 在 `PlaybackRepository.loadGatewaySafely` 中通过
 * [SongUrlResolver] 懒加载。这样可以把"能否解析"与"能否入队/收藏"解耦，收藏/歌单
 * 里保留的是"意图播放这首在线歌"，而不是某个可能过期的 CDN URL。
 *
 * [asOnlineMusicIdOrNull] 从一个 Track 反解出 [OnlineMusicId]，用于收藏/歌单读回时判断
 * 这条 Track 是"本地条目"还是"在线条目"。仅当 [Track.mediaLocator] 具备 `online-lazy://`
 * scheme 才返回非空。
 */

private const val ONLINE_LAZY_SCHEME = "online-lazy://"

fun OnlineSong.toTrack(
    preferredQuality: String = defaultQuality.lxKey,
): Track {
    val source = id.source
    val songmid = id.songmid
    return Track(
        id = onlineTrackId(source = source, songmid = songmid),
        sourceId = onlineSourceId(source = source),
        title = name,
        artistName = singer.takeIf { it.isNotBlank() },
        albumTitle = album?.takeIf { it.isNotBlank() },
        durationMs = intervalSeconds.coerceAtLeast(0).toLong() * 1000L,
        trackNumber = null,
        discNumber = null,
        mediaLocator = buildOnlineLazyLocator(
            source = source,
            songmid = songmid,
            quality = preferredQuality,
        ),
        relativePath = "",
        artworkLocator = coverUrl?.takeIf { it.isNotBlank() },
        sizeBytes = 0L,
        modifiedAt = 0L,
    )
}

fun Track.asOnlineMusicIdOrNull(): OnlineMusicId? {
    val locator = mediaLocator
    if (!locator.startsWith(ONLINE_LAZY_SCHEME)) return null
    val remainder = locator.substring(ONLINE_LAZY_SCHEME.length)
    val queryIndex = remainder.indexOf('?')
    val pathPart = if (queryIndex >= 0) remainder.substring(0, queryIndex) else remainder
    val slashIndex = pathPart.indexOf('/')
    if (slashIndex <= 0 || slashIndex == pathPart.lastIndex) return null
    val source = pathPart.substring(0, slashIndex)
    val songmid = pathPart.substring(slashIndex + 1)
    if (source.isBlank() || songmid.isBlank()) return null
    return OnlineMusicId(source = source, songmid = songmid)
}

/**
 * 在线歌曲在 Track 层的稳定 id（去重/收藏 key）。形如 `online-kw-123`。
 */
fun onlineTrackId(source: String, songmid: String): String = "online-$source-$songmid"

/**
 * 在线歌曲在 Track 层的 sourceId。形如 `online-kw`；用于区分本地 import source。
 */
fun onlineSourceId(source: String): String = "online-$source"

/**
 * 拼出 lazy locator：`online-lazy://<source>/<songmid>?q=<quality>`
 */
fun buildOnlineLazyLocator(source: String, songmid: String, quality: String): String {
    return "online-lazy://$source/$songmid?q=$quality"
}

/**
 * 便捷重载：允许直接传 [Quality] 枚举。
 */
fun OnlineSong.toTrack(preferredQuality: Quality): Track =
    toTrack(preferredQuality = preferredQuality.lxKey)
