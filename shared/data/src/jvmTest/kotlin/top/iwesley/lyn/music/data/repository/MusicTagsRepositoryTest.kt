package top.iwesley.lyn.music.data.repository

import androidx.room.Room
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import top.iwesley.lyn.music.core.model.AudioTagGateway
import top.iwesley.lyn.music.core.model.AudioTagPatch
import top.iwesley.lyn.music.core.model.AudioTagSnapshot
import top.iwesley.lyn.music.core.model.Track
import top.iwesley.lyn.music.data.db.ImportSourceEntity
import top.iwesley.lyn.music.data.db.LynMusicDatabase
import top.iwesley.lyn.music.data.db.TrackEntity
import top.iwesley.lyn.music.data.db.buildLynMusicDatabase

class MusicTagsRepositoryTest {

    @Test
    fun `local tracks flow only includes local folder sources`() = runTest {
        val database = createMusicTagsTestDatabase()
        seedSource(database, id = "local-1", type = "LOCAL_FOLDER")
        seedSource(database, id = "smb-1", type = "SAMBA")
        seedTrack(database, id = "local-track", sourceId = "local-1", title = "本地歌")
        seedTrack(database, id = "remote-track", sourceId = "smb-1", title = "远程歌")
        val repository = RoomMusicTagsRepository(database, FakeAudioTagGateway())

        val tracks = repository.localTracks.first()

        assertEquals(listOf("local-track"), tracks.map { it.id })
    }

    @Test
    fun `save tags updates track fields and rebuilds summaries`() = runTest {
        val database = createMusicTagsTestDatabase()
        seedSource(database, id = "local-1", type = "LOCAL_FOLDER")
        seedTrack(
            database = database,
            id = "track-1",
            sourceId = "local-1",
            title = "旧标题",
            artistName = "旧艺人",
            albumTitle = "旧专辑",
            trackNumber = 1,
            discNumber = 1,
        )
        val gateway = FakeAudioTagGateway(
            writeSnapshots = mapOf(
                "track-1" to AudioTagSnapshot(
                    title = "新标题",
                    artistName = "新艺人",
                    albumTitle = "新专辑",
                    albumArtist = "新艺人",
                    year = 2024,
                    genre = "Pop",
                    comment = "updated",
                    composer = "composer",
                    isCompilation = false,
                    tagLabel = "ID3v2.4",
                    trackNumber = 7,
                    discNumber = 2,
                    artworkLocator = "/tmp/new-artwork.png",
                ),
            ),
        )
        val repository = RoomMusicTagsRepository(database, gateway)
        val original = database.trackDao().getByIds(listOf("track-1")).first().toDomain()

        repository.saveTags(
            track = original,
            patch = AudioTagPatch(
                title = "新标题",
                artistName = "新艺人",
                albumTitle = "新专辑",
                trackNumber = 7,
                discNumber = 2,
            ),
        ).getOrThrow()

        val saved = database.trackDao().getByIds(listOf("track-1")).first()
        assertEquals("新标题", saved.title)
        assertEquals("新艺人", saved.artistName)
        assertEquals("新专辑", saved.albumTitle)
        assertEquals(7, saved.trackNumber)
        assertEquals(2, saved.discNumber)
        assertEquals("/tmp/new-artwork.png", saved.artworkLocator)

        val artists = database.artistDao().observeAll().first()
        val albums = database.albumDao().observeAll().first()
        assertEquals(listOf("新艺人"), artists.map { it.name })
        assertEquals(listOf("新专辑"), albums.map { it.title })
    }
}

private class FakeAudioTagGateway(
    private val readSnapshots: Map<String, AudioTagSnapshot> = emptyMap(),
    private val writeSnapshots: Map<String, AudioTagSnapshot> = readSnapshots,
) : AudioTagGateway {
    override suspend fun canEdit(track: Track): Boolean = true

    override suspend fun canWrite(track: Track): Boolean = true

    override suspend fun read(track: Track): Result<AudioTagSnapshot> {
        return readSnapshots[track.id]?.let(Result.Companion::success)
            ?: Result.failure(IllegalStateException("missing snapshot"))
    }

    override suspend fun write(track: Track, patch: AudioTagPatch): Result<AudioTagSnapshot> {
        return writeSnapshots[track.id]?.let(Result.Companion::success)
            ?: Result.failure(IllegalStateException("missing snapshot"))
    }
}

private fun createMusicTagsTestDatabase(): LynMusicDatabase {
    val path = Files.createTempFile("lynmusic-tags", ".db")
    return buildLynMusicDatabase(
        Room.databaseBuilder<LynMusicDatabase>(name = path.absolutePathString()),
    )
}

private suspend fun seedSource(
    database: LynMusicDatabase,
    id: String,
    type: String,
) {
    database.importSourceDao().upsert(
        ImportSourceEntity(
            id = id,
            type = type,
            label = id,
            rootReference = "/music",
            server = null,
            shareName = null,
            directoryPath = null,
            username = null,
            credentialKey = null,
            allowInsecureTls = false,
            lastScannedAt = null,
            createdAt = 1L,
        ),
    )
}

private suspend fun seedTrack(
    database: LynMusicDatabase,
    id: String,
    sourceId: String,
    title: String,
    artistName: String? = null,
    albumTitle: String? = null,
    trackNumber: Int? = null,
    discNumber: Int? = null,
) {
    database.trackDao().upsertAll(
        listOf(
            TrackEntity(
                id = id,
                sourceId = sourceId,
                title = title,
                artistId = artistName?.let(::artistIdForLibraryMetadata),
                artistName = artistName,
                albumId = albumTitle?.let { albumIdForLibraryMetadata(artistName, it) },
                albumTitle = albumTitle,
                durationMs = 200_000,
                trackNumber = trackNumber,
                discNumber = discNumber,
                mediaLocator = "/music/$title.mp3",
                relativePath = "$title.mp3",
                artworkLocator = null,
                sizeBytes = 100L,
                modifiedAt = 1L,
            ),
        ),
    )
}
