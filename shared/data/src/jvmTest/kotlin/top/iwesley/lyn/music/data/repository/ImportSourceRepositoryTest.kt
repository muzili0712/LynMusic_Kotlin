package top.iwesley.lyn.music.data.repository

import androidx.room.Room
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import top.iwesley.lyn.music.core.model.ImportScanReport
import top.iwesley.lyn.music.core.model.ImportSourceGateway
import top.iwesley.lyn.music.core.model.ImportSourceType
import top.iwesley.lyn.music.core.model.LocalFolderSelection
import top.iwesley.lyn.music.core.model.NavidromeSourceDraft
import top.iwesley.lyn.music.core.model.SambaSourceDraft
import top.iwesley.lyn.music.core.model.SecureCredentialStore
import top.iwesley.lyn.music.core.model.WebDavSourceDraft
import top.iwesley.lyn.music.core.model.normalizeWebDavRootUrl
import top.iwesley.lyn.music.data.db.ImportSourceEntity
import top.iwesley.lyn.music.data.db.LynMusicDatabase
import top.iwesley.lyn.music.data.db.buildLynMusicDatabase

class ImportSourceRepositoryTest {

    @Test
    fun `add source rejects duplicate names ignoring case and whitespace`() = runTest {
        val database = createImportTestDatabase()
        database.importSourceDao().upsert(
            importSourceEntity(
                id = "local-1",
                type = ImportSourceType.LOCAL_FOLDER,
                label = " 我的音乐源 ",
                rootReference = "folder://music",
            ),
        )
        val repository = createRepository(database = database)

        val result = repository.addWebDavSource(
            WebDavSourceDraft(
                label = "我的音乐源",
                rootUrl = "https://dav.example.com/music",
                username = "",
                password = "",
            ),
        )

        assertEquals("音乐源名称已存在。", result.exceptionOrNull()?.message)
        assertEquals(1, database.importSourceDao().getAll().size)
    }

    @Test
    fun `import local folder rejects duplicate persistent reference`() = runTest {
        val database = createImportTestDatabase()
        database.importSourceDao().upsert(
            importSourceEntity(
                id = "local-1",
                type = ImportSourceType.LOCAL_FOLDER,
                label = "下载目录",
                rootReference = "folder://downloads",
            ),
        )
        val gateway = RecordingImportSourceGateway(
            nextLocalFolderSelection = LocalFolderSelection(
                label = "另一个目录",
                persistentReference = "folder://downloads",
            ),
        )
        val repository = createRepository(database = database, gateway = gateway)

        val result = repository.importLocalFolder()

        assertEquals("该本地文件夹已导入。", result.exceptionOrNull()?.message)
        assertEquals(1, database.importSourceDao().getAll().size)
        assertEquals(0, gateway.localFolderScanCount)
    }

    @Test
    fun `import local folder rejects duplicate name even when path differs`() = runTest {
        val database = createImportTestDatabase()
        database.importSourceDao().upsert(
            importSourceEntity(
                id = "dav-1",
                type = ImportSourceType.WEBDAV,
                label = " 下载目录 ",
                rootReference = "https://dav.example.com/music",
            ),
        )
        val repository = createRepository(
            database = database,
            gateway = RecordingImportSourceGateway(
                nextLocalFolderSelection = LocalFolderSelection(
                    label = "下载目录",
                    persistentReference = "folder://new-downloads",
                ),
            ),
        )

        val result = repository.importLocalFolder()

        assertEquals("音乐源名称已存在。", result.exceptionOrNull()?.message)
        assertEquals(1, database.importSourceDao().getAll().size)
    }

    @Test
    fun `local folder path conflict only checks local folder sources`() = runTest {
        val database = createImportTestDatabase()
        database.importSourceDao().upsert(
            importSourceEntity(
                id = "dav-1",
                type = ImportSourceType.WEBDAV,
                label = "云端曲库",
                rootReference = "folder://downloads",
            ),
        )
        val gateway = RecordingImportSourceGateway(
            nextLocalFolderSelection = LocalFolderSelection(
                label = "下载目录",
                persistentReference = "folder://downloads",
            ),
        )
        val repository = createRepository(database = database, gateway = gateway)

        val result = repository.importLocalFolder()

        assertTrue(result.isSuccess)
        assertEquals(2, database.importSourceDao().getAll().size)
        assertEquals(1, gateway.localFolderScanCount)
    }

    @Test
    fun `different names and local folder paths can both be added`() = runTest {
        val database = createImportTestDatabase()
        val gateway = RecordingImportSourceGateway(
            nextLocalFolderSelection = LocalFolderSelection(
                label = "下载目录",
                persistentReference = "folder://downloads",
            ),
        )
        val repository = createRepository(database = database, gateway = gateway)

        assertTrue(repository.importLocalFolder().isSuccess)
        assertTrue(
            repository.addSambaSource(
                SambaSourceDraft(
                    label = "家庭 NAS",
                    server = "nas.local",
                    path = "Media/Music",
                    username = "",
                    password = "",
                ),
            ).isSuccess,
        )

        assertEquals(2, database.importSourceDao().getAll().size)
        assertEquals(1, gateway.localFolderScanCount)
        assertEquals(1, gateway.sambaScanCount)
    }

    @Test
    fun `blank label sources validate against generated fallback labels`() = runTest {
        val database = createImportTestDatabase()
        val normalizedRootUrl = normalizeWebDavRootUrl("https://dav.example.com/music/")
        database.importSourceDao().upsert(
            importSourceEntity(
                id = "local-1",
                type = ImportSourceType.LOCAL_FOLDER,
                label = normalizedRootUrl,
                rootReference = "folder://downloads",
            ),
        )
        val repository = createRepository(database = database)

        val result = repository.addWebDavSource(
            WebDavSourceDraft(
                label = " ",
                rootUrl = "https://dav.example.com/music/",
                username = "",
                password = "",
            ),
        )

        assertEquals("音乐源名称已存在。", result.exceptionOrNull()?.message)
        assertEquals(1, database.importSourceDao().getAll().size)
    }
}

private fun createRepository(
    database: LynMusicDatabase,
    gateway: RecordingImportSourceGateway = RecordingImportSourceGateway(),
): RoomImportSourceRepository {
    return RoomImportSourceRepository(
        database = database,
        gateway = gateway,
        secureCredentialStore = ImportTestSecureCredentialStore(),
    )
}

private fun createImportTestDatabase(): LynMusicDatabase {
    val path = Files.createTempFile("lynmusic-import-sources", ".db")
    return buildLynMusicDatabase(
        Room.databaseBuilder<LynMusicDatabase>(name = path.absolutePathString()),
    )
}

private fun importSourceEntity(
    id: String,
    type: ImportSourceType,
    label: String,
    rootReference: String,
): ImportSourceEntity {
    return ImportSourceEntity(
        id = id,
        type = type.name,
        label = label,
        rootReference = rootReference,
        server = null,
        shareName = null,
        directoryPath = null,
        username = null,
        credentialKey = null,
        allowInsecureTls = false,
        lastScannedAt = null,
        createdAt = 1L,
    )
}

private class RecordingImportSourceGateway(
    var nextLocalFolderSelection: LocalFolderSelection? = null,
    private val scanReport: ImportScanReport = ImportScanReport(tracks = emptyList()),
) : ImportSourceGateway {
    var localFolderScanCount: Int = 0
    var sambaScanCount: Int = 0
    var webDavScanCount: Int = 0
    var navidromeScanCount: Int = 0

    override suspend fun pickLocalFolder(): LocalFolderSelection? = nextLocalFolderSelection

    override suspend fun scanLocalFolder(selection: LocalFolderSelection, sourceId: String): ImportScanReport {
        localFolderScanCount += 1
        return scanReport
    }

    override suspend fun scanSamba(draft: SambaSourceDraft, sourceId: String): ImportScanReport {
        sambaScanCount += 1
        return scanReport
    }

    override suspend fun scanWebDav(draft: WebDavSourceDraft, sourceId: String): ImportScanReport {
        webDavScanCount += 1
        return scanReport
    }

    override suspend fun scanNavidrome(draft: NavidromeSourceDraft, sourceId: String): ImportScanReport {
        navidromeScanCount += 1
        return scanReport
    }
}

private class ImportTestSecureCredentialStore(
    private val values: MutableMap<String, String> = linkedMapOf(),
) : SecureCredentialStore {
    override suspend fun put(key: String, value: String) {
        values[key] = value
    }

    override suspend fun get(key: String): String? = values[key]

    override suspend fun remove(key: String) {
        values.remove(key)
    }
}
