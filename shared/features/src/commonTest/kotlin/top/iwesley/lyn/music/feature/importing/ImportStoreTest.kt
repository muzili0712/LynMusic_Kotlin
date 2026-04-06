package top.iwesley.lyn.music.feature.importing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import top.iwesley.lyn.music.core.model.NavidromeSourceDraft
import top.iwesley.lyn.music.core.model.PlatformCapabilities
import top.iwesley.lyn.music.core.model.SambaSourceDraft
import top.iwesley.lyn.music.core.model.SourceWithStatus
import top.iwesley.lyn.music.core.model.WebDavSourceDraft
import top.iwesley.lyn.music.data.repository.ImportSourceRepository

@OptIn(ExperimentalCoroutinesApi::class)
class ImportStoreTest {

    @Test
    fun `name conflict failure is surfaced through existing samba error message`() = runTest {
        val repository = FakeImportSourceRepository(
            sambaResult = Result.failure(IllegalStateException("音乐源名称已存在。")),
        )
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler) + SupervisorJob())
        val store = ImportStore(
            repository = repository,
            capabilities = testPlatformCapabilities(),
            scope = scope,
        )

        store.dispatch(ImportIntent.SambaServerChanged("nas.local"))
        store.dispatch(ImportIntent.SambaPathChanged("Media/Music"))
        advanceUntilIdle()
        store.dispatch(ImportIntent.AddSambaSource)
        advanceUntilIdle()

        assertEquals("Samba 导入失败: 音乐源名称已存在。", store.state.value.message)
        scope.cancel()
    }

    @Test
    fun `local folder path conflict is surfaced through existing import message`() = runTest {
        val repository = FakeImportSourceRepository(
            localFolderResult = Result.failure(IllegalStateException("该本地文件夹已导入。")),
        )
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler) + SupervisorJob())
        val store = ImportStore(
            repository = repository,
            capabilities = testPlatformCapabilities(),
            scope = scope,
        )

        store.dispatch(ImportIntent.ImportLocalFolder)
        advanceUntilIdle()

        assertEquals("导入本地文件夹失败: 该本地文件夹已导入。", store.state.value.message)
        scope.cancel()
    }
}

private fun testPlatformCapabilities(): PlatformCapabilities {
    return PlatformCapabilities(
        supportsLocalFolderImport = true,
        supportsSambaImport = true,
        supportsWebDavImport = true,
        supportsNavidromeImport = true,
        supportsSystemMediaControls = true,
    )
}

private class FakeImportSourceRepository(
    localFolderResult: Result<Unit> = Result.success(Unit),
    sambaResult: Result<Unit> = Result.success(Unit),
    private val webDavResult: Result<Unit> = Result.success(Unit),
    private val navidromeResult: Result<Unit> = Result.success(Unit),
    sources: List<SourceWithStatus> = emptyList(),
) : ImportSourceRepository {
    private val mutableSources = MutableStateFlow(sources)
    private val localFolderResult = localFolderResult
    private val sambaResult = sambaResult

    override fun observeSources(): Flow<List<SourceWithStatus>> = mutableSources.asStateFlow()

    override suspend fun importLocalFolder(): Result<Unit> = localFolderResult

    override suspend fun addSambaSource(draft: SambaSourceDraft): Result<Unit> = sambaResult

    override suspend fun addWebDavSource(draft: WebDavSourceDraft): Result<Unit> = webDavResult

    override suspend fun addNavidromeSource(draft: NavidromeSourceDraft): Result<Unit> = navidromeResult

    override suspend fun rescanSource(sourceId: String): Result<Unit> = Result.success(Unit)

    override suspend fun deleteSource(sourceId: String): Result<Unit> = Result.success(Unit)
}
