package top.iwesley.lyn.music.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlin.time.Clock
import kotlin.time.Instant
import top.iwesley.lyn.music.core.model.DiagnosticLogger
import top.iwesley.lyn.music.core.model.ImportSourceType
import top.iwesley.lyn.music.core.model.LyricsHttpClient
import top.iwesley.lyn.music.core.model.NoopDiagnosticLogger
import top.iwesley.lyn.music.core.model.SecureCredentialStore
import top.iwesley.lyn.music.core.model.Track
import top.iwesley.lyn.music.core.model.error
import top.iwesley.lyn.music.core.model.info
import top.iwesley.lyn.music.core.model.parseNavidromeSongLocator
import top.iwesley.lyn.music.core.model.warn
import top.iwesley.lyn.music.data.db.FavoriteTrackEntity
import top.iwesley.lyn.music.data.db.ImportSourceEntity
import top.iwesley.lyn.music.data.db.LynMusicDatabase
import top.iwesley.lyn.music.data.db.OnlineSongEntity
import top.iwesley.lyn.music.domain.NavidromeResolvedSource
import top.iwesley.lyn.music.domain.normalizeNavidromeBaseUrl
import top.iwesley.lyn.music.domain.requestNavidromeJson

interface FavoritesRepository {
    val favoriteTrackIds: Flow<Set<String>>
    val favoriteTracks: Flow<List<Track>>

    suspend fun toggleFavorite(track: Track): Result<Boolean>
    suspend fun setFavorite(track: Track, favorite: Boolean): Result<Boolean>
    suspend fun refreshNavidromeFavorites(): Result<Unit>

    /**
     * T10: 在线歌曲收藏切换。
     *
     * 实现：存在则删除返回 false；不存在则把 [record] upsert 进 `online_song` 缓存表 + 写入
     * `favorite_track(origin = ONLINE)` 返回 true。Track 的 id 依据 [OnlineSongRecord.trackId] / sourceId。
     *
     * 读回在线收藏（让它出现在 Favorites 列表）是 M1 的范围，M0 只保证"入库"。
     */
    suspend fun toggleFavoriteOnline(record: OnlineSongRecord): Result<Boolean>
}

/**
 * T10: 跨模块传递在线歌曲元数据的精简 DTO。
 *
 * shared:data 不能反向依赖 shared:online（后者已依赖前者），所以不能直接在 Repository
 * 接口里引用 `OnlineSong`。Store 层拿到 OnlineSong 后组装 [OnlineSongRecord] 传入。
 *
 * [trackId] 形如 `online-${source}-${songmid}`；[sourceId] 形如 `online-${source}`；
 * 二者由上层（OnlineSongToTrack adapter）保证与 Track 的 id/sourceId 对齐。
 */
data class OnlineSongRecord(
    val trackId: String,
    val sourceId: String,
    val source: String,
    val songmid: String,
    val name: String,
    val singer: String,
    val album: String?,
    val intervalSeconds: Int,
    val coverUrl: String?,
    val defaultQualityKey: String,
)

class RoomFavoritesRepository(
    private val database: LynMusicDatabase,
    private val secureCredentialStore: SecureCredentialStore,
    private val httpClient: LyricsHttpClient,
    private val logger: DiagnosticLogger = NoopDiagnosticLogger,
) : FavoritesRepository {
    private val favoriteRows = database.favoriteTrackDao().observeAll()

    override val favoriteTrackIds: Flow<Set<String>> = favoriteRows
        .map { rows -> rows.mapTo(linkedSetOf()) { it.trackId } }

    override val favoriteTracks: Flow<List<Track>> = combine(
        favoriteRows,
        database.trackDao().observeAll(),
        database.importSourceDao().observeAll(),
        database.lyricsCacheDao().observeBySourceId(MANUAL_LYRICS_OVERRIDE_SOURCE_ID),
    ) { favorites, tracks, sources, overrides ->
        val enabledSourceIds = sources.asSequence()
            .filter { it.enabled }
            .map { it.id }
            .toSet()
        val artworkOverrides = manualArtworkOverridesByTrackId(overrides)
        val trackById = tracks
            .filter { it.sourceId in enabledSourceIds }
            .associate { track ->
                track.id to track.toDomain(artworkOverrides[track.id])
            }
        favorites.mapNotNull { trackById[it.trackId] }
    }

    override suspend fun toggleFavorite(track: Track): Result<Boolean> {
        val favorite = database.favoriteTrackDao().getByTrackId(track.id) == null
        return setFavorite(track, favorite)
    }

    override suspend fun setFavorite(track: Track, favorite: Boolean): Result<Boolean> {
        return runCatching {
            val existing = database.favoriteTrackDao().getByTrackId(track.id)
            if ((existing != null) == favorite) {
                return@runCatching favorite
            }
            val navidromeSongId = parseNavidromeSongLocator(track.mediaLocator)
                ?.takeIf { it.first == track.sourceId }
                ?.second
            if (navidromeSongId != null) {
                setNavidromeFavorite(track, navidromeSongId, existing, favorite)
            } else {
                setLocalFavorite(track, existing, favorite)
            }
        }
    }

    override suspend fun toggleFavoriteOnline(record: OnlineSongRecord): Result<Boolean> {
        return runCatching {
            val existing = database.favoriteTrackDao().getByTrackId(record.trackId)
            if (existing != null) {
                database.favoriteTrackDao().deleteByTrackId(record.trackId)
                logger.info(FAVORITES_LOG_TAG) {
                    "unfavorite-online track=${record.trackId} source=${record.source} songmid=${record.songmid}"
                }
                false
            } else {
                val nowMs = favoriteNow()
                database.onlineSongDao().upsert(record.toEntity(nowMs = nowMs))
                database.favoriteTrackDao().upsert(
                    FavoriteTrackEntity(
                        trackId = record.trackId,
                        sourceId = record.sourceId,
                        remoteSongId = null,
                        favoritedAt = nowMs,
                        origin = ORIGIN_ONLINE,
                    ),
                )
                logger.info(FAVORITES_LOG_TAG) {
                    "favorite-online track=${record.trackId} source=${record.source} songmid=${record.songmid}"
                }
                true
            }
        }
    }

    override suspend fun refreshNavidromeFavorites(): Result<Unit> {
        return runCatching {
            val failures = mutableListOf<String>()
            database.importSourceDao().getAll()
                .filter { it.type == ImportSourceType.NAVIDROME.name && it.enabled }
                .forEach { source ->
                    runCatching { syncNavidromeFavorites(source) }
                        .onFailure { throwable ->
                            val message = "${source.label}: ${throwable.message.orEmpty()}"
                            failures += message
                            logger.error(FAVORITES_LOG_TAG, throwable) {
                                "refresh-failed source=${source.id} label=${source.label}"
                            }
                        }
                }
            if (failures.isNotEmpty()) {
                error(failures.joinToString("\n"))
            }
        }
    }

    private suspend fun setLocalFavorite(
        track: Track,
        existing: FavoriteTrackEntity?,
        favorite: Boolean,
    ): Boolean {
        if (!favorite) {
            database.favoriteTrackDao().deleteByTrackId(track.id)
            logger.info(FAVORITES_LOG_TAG) { "unfavorite-local track=${track.id} source=${track.sourceId}" }
            return false
        }
        database.favoriteTrackDao().upsert(
            FavoriteTrackEntity(
                trackId = track.id,
                sourceId = track.sourceId,
                remoteSongId = null,
                favoritedAt = favoriteNow(),
            ),
        )
        logger.info(FAVORITES_LOG_TAG) { "favorite-local track=${track.id} source=${track.sourceId}" }
        return true
    }

    private suspend fun setNavidromeFavorite(
        track: Track,
        remoteSongId: String,
        existing: FavoriteTrackEntity?,
        favorite: Boolean,
    ): Boolean {
        val resolvedSource = resolveNavidromeSource(track.sourceId)
            ?: error("Navidrome 来源不可用，无法更新喜欢状态。")
        val endpoint = if (favorite) "star" else "unstar"
        requestNavidromeJson(
            httpClient = httpClient,
            source = resolvedSource,
            endpoint = endpoint,
            parameters = mapOf("id" to remoteSongId),
        )
        return if (!favorite) {
            database.favoriteTrackDao().deleteByTrackId(track.id)
            logger.info(FAVORITES_LOG_TAG) { "unfavorite-navidrome track=${track.id} source=${track.sourceId} song=$remoteSongId" }
            false
        } else {
            database.favoriteTrackDao().upsert(
                FavoriteTrackEntity(
                    trackId = track.id,
                    sourceId = track.sourceId,
                    remoteSongId = remoteSongId,
                    favoritedAt = favoriteNow(),
                ),
            )
            logger.info(FAVORITES_LOG_TAG) { "favorite-navidrome track=${track.id} source=${track.sourceId} song=$remoteSongId" }
            true
        }
    }

    private suspend fun syncNavidromeFavorites(source: ImportSourceEntity) {
        val resolved = source.toNavidromeResolvedSource()
            ?: error("Navidrome 来源缺少有效账号或密码，无法同步喜欢。")
        val payload = requestNavidromeJson(
            httpClient = httpClient,
            source = resolved,
            endpoint = "getStarred2",
        )
        val existingRows = database.favoriteTrackDao().getBySourceId(source.id)
        val existingByRemoteSongId = existingRows
            .mapNotNull { entity -> entity.remoteSongId?.let { it to entity } }
            .toMap()
        val syncedSongs = payload["starred2"].asJsonObjectOrNull()
            ?.get("song")
            .asJsonObjectList()
        val newSongIds = syncedSongs
            .mapNotNull { song -> song.string("id") }
            .filterNot(existingByRemoteSongId::containsKey)
        val maxExistingFavoritedAt = existingRows.maxOfOrNull { it.favoritedAt }
        var nextNewFavoritedAt = maxOf(
            favoriteNow(),
            (maxExistingFavoritedAt ?: Long.MIN_VALUE) + newSongIds.size.toLong(),
        )
        val favoriteRows = syncedSongs
            .mapNotNull { song ->
                val songId = song.string("id") ?: return@mapNotNull null
                FavoriteTrackEntity(
                    trackId = navidromeTrackIdFor(source.id, songId),
                    sourceId = source.id,
                    remoteSongId = songId,
                    favoritedAt = song.string("starred")?.let(::parseFavoriteTimestampMillis)
                        ?: existingByRemoteSongId[songId]?.favoritedAt
                        ?: nextNewFavoritedAt--,
                )
            }
        database.favoriteTrackDao().deleteBySourceId(source.id)
        if (favoriteRows.isNotEmpty()) {
            database.favoriteTrackDao().upsertAll(favoriteRows)
        }
        logger.info(FAVORITES_LOG_TAG) { "refresh-complete source=${source.id} favorites=${favoriteRows.size}" }
    }

    private suspend fun resolveNavidromeSource(sourceId: String): NavidromeResolvedSource? {
        val source = database.importSourceDao().getById(sourceId)
            ?.takeIf { it.type == ImportSourceType.NAVIDROME.name && it.enabled }
            ?: return null
        return source.toNavidromeResolvedSource()
    }

    private suspend fun ImportSourceEntity.toNavidromeResolvedSource(): NavidromeResolvedSource? {
        val username = username?.trim().orEmpty()
        val password = credentialKey?.let { secureCredentialStore.get(it) }.orEmpty()
        if (username.isBlank() || password.isBlank()) return null
        return NavidromeResolvedSource(
            baseUrl = normalizeNavidromeBaseUrl(rootReference),
            username = username,
            password = password,
        )
    }

    private companion object {
        const val FAVORITES_LOG_TAG = "Favorites"
        const val ORIGIN_ONLINE = "ONLINE"
    }
}

/**
 * T10 helper：把跨模块的 [OnlineSongRecord] DTO 映射到 Room 实体。[nowMs] 用于 createdAt。
 */
internal fun OnlineSongRecord.toEntity(nowMs: Long): OnlineSongEntity {
    return OnlineSongEntity(
        source = source,
        songmid = songmid,
        name = name,
        singer = singer,
        album = album,
        intervalSeconds = intervalSeconds.coerceAtLeast(0),
        coverUrl = coverUrl,
        defaultQuality = defaultQualityKey,
        createdAt = nowMs,
    )
}

private fun JsonElement?.asJsonObjectOrNull(): JsonObject? = this as? JsonObject

private fun JsonElement?.asJsonObjectList(): List<JsonObject> {
    return when (val element = this) {
        is JsonArray -> element.mapNotNull { it as? JsonObject }
        is JsonObject -> listOf(element)
        else -> emptyList()
    }
}

private fun JsonObject.string(key: String): String? {
    return (this[key] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }
}

private fun parseFavoriteTimestampMillis(value: String): Long? {
    return runCatching { Instant.parse(value).toEpochMilliseconds() }.getOrNull()
}

private fun favoriteNow(): Long = Clock.System.now().toEpochMilliseconds()
