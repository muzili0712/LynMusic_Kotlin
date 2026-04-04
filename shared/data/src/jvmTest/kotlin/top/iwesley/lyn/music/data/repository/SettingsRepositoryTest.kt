package top.iwesley.lyn.music.data.repository

import androidx.room.Room
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import top.iwesley.lyn.music.core.model.LyricsResponseFormat
import top.iwesley.lyn.music.core.model.LyricsSourceConfig
import top.iwesley.lyn.music.core.model.RequestMethod
import top.iwesley.lyn.music.core.model.SambaCachePreferencesStore
import top.iwesley.lyn.music.data.db.LyricsSourceConfigEntity
import top.iwesley.lyn.music.data.db.LynMusicDatabase
import top.iwesley.lyn.music.data.db.WorkflowLyricsSourceConfigEntity
import top.iwesley.lyn.music.data.db.buildLynMusicDatabase

class SettingsRepositoryTest {

    @Test
    fun `saving direct source rejects duplicate direct name ignoring case and trim`() = runTest {
        val database = createSettingsTestDatabase()
        database.lyricsSourceConfigDao().upsert(directEntity(id = "direct-1", name = "LRCLIB"))
        val repository = DefaultSettingsRepository(database, FakeSambaCachePreferencesStore())

        val error = assertFailsWith<IllegalStateException> {
            repository.saveLyricsSource(
                directConfig(id = "direct-2", name = " lrclib "),
            )
        }

        assertEquals("歌词源名称已存在。", error.message)
    }

    @Test
    fun `saving direct source rejects duplicate workflow name`() = runTest {
        val database = createSettingsTestDatabase()
        database.workflowLyricsSourceConfigDao().upsert(workflowEntity(id = "wf-1", name = "QQ Lyrics"))
        val repository = DefaultSettingsRepository(database, FakeSambaCachePreferencesStore())

        val error = assertFailsWith<IllegalStateException> {
            repository.saveLyricsSource(
                directConfig(id = "direct-2", name = " qq lyrics "),
            )
        }

        assertEquals("歌词源名称已存在。", error.message)
    }

    @Test
    fun `editing direct source can keep original name`() = runTest {
        val database = createSettingsTestDatabase()
        database.lyricsSourceConfigDao().upsert(directEntity(id = "direct-1", name = "My Lyrics"))
        val repository = DefaultSettingsRepository(database, FakeSambaCachePreferencesStore())

        repository.saveLyricsSource(
            directConfig(
                id = "direct-1",
                name = " my lyrics ",
                urlTemplate = "https://lyrics.example/v2",
            ),
        )

        val saved = database.lyricsSourceConfigDao().getAll().single()
        assertEquals(" my lyrics ", saved.name)
        assertEquals("https://lyrics.example/v2", saved.urlTemplate)
    }

    @Test
    fun `saving workflow rejects duplicate name across all lyrics sources`() = runTest {
        val database = createSettingsTestDatabase()
        database.lyricsSourceConfigDao().upsert(directEntity(id = "direct-1", name = "Shared Name"))
        val repository = DefaultSettingsRepository(database, FakeSambaCachePreferencesStore())

        val error = assertFailsWith<IllegalStateException> {
            repository.saveWorkflowLyricsSource(
                rawJson = workflowJson(id = "wf-1", name = " shared name "),
                editingId = null,
            )
        }

        assertEquals("歌词源名称已存在。", error.message)
    }

    @Test
    fun `editing workflow can rename to unique name`() = runTest {
        val database = createSettingsTestDatabase()
        database.workflowLyricsSourceConfigDao().upsert(workflowEntity(id = "wf-1", name = "Old Workflow"))
        val repository = DefaultSettingsRepository(database, FakeSambaCachePreferencesStore())

        val saved = repository.saveWorkflowLyricsSource(
            rawJson = workflowJson(id = "wf-1", name = "New Workflow"),
            editingId = "wf-1",
        )

        assertEquals("wf-1", saved.id)
        assertEquals("New Workflow", saved.name)
        assertEquals("New Workflow", database.workflowLyricsSourceConfigDao().getById("wf-1")?.name)
    }

    @Test
    fun `editing workflow cannot change id`() = runTest {
        val database = createSettingsTestDatabase()
        database.workflowLyricsSourceConfigDao().upsert(workflowEntity(id = "wf-1", name = "Old Workflow"))
        val repository = DefaultSettingsRepository(database, FakeSambaCachePreferencesStore())

        val error = assertFailsWith<IllegalStateException> {
            repository.saveWorkflowLyricsSource(
                rawJson = workflowJson(id = "wf-2", name = "Renamed Workflow"),
                editingId = "wf-1",
            )
        }

        assertEquals("Workflow 源 id 不支持修改。", error.message)
    }
}

private fun createSettingsTestDatabase(): LynMusicDatabase {
    val path = Files.createTempFile("lynmusic-settings", ".db")
    return buildLynMusicDatabase(
        Room.databaseBuilder<LynMusicDatabase>(name = path.absolutePathString()),
    )
}

private class FakeSambaCachePreferencesStore : SambaCachePreferencesStore {
    override val useSambaCache = MutableStateFlow(true)

    override suspend fun setUseSambaCache(enabled: Boolean) {
        useSambaCache.value = enabled
    }
}

private fun directConfig(
    id: String,
    name: String,
    urlTemplate: String = "https://lyrics.example/api",
): LyricsSourceConfig {
    return LyricsSourceConfig(
        id = id,
        name = name,
        method = RequestMethod.GET,
        urlTemplate = urlTemplate,
        responseFormat = LyricsResponseFormat.JSON,
        extractor = "json-map:lyrics=plainLyrics,title=trackName",
        priority = 0,
        enabled = true,
    )
}

private fun directEntity(
    id: String,
    name: String,
): LyricsSourceConfigEntity {
    return LyricsSourceConfigEntity(
        id = id,
        name = name,
        method = "GET",
        urlTemplate = "https://lyrics.example/api",
        headersTemplate = "",
        queryTemplate = "",
        bodyTemplate = "",
        responseFormat = "JSON",
        extractor = "json-map:lyrics=plainLyrics,title=trackName",
        priority = 0,
        enabled = true,
    )
}

private fun workflowEntity(
    id: String,
    name: String,
): WorkflowLyricsSourceConfigEntity {
    return WorkflowLyricsSourceConfigEntity(
        id = id,
        name = name,
        priority = 0,
        enabled = true,
        rawJson = workflowJson(id = id, name = name),
    )
}

private fun workflowJson(
    id: String,
    name: String,
): String {
    return """
        {
          "id": "$id",
          "name": "$name",
          "kind": "workflow",
          "enabled": true,
          "priority": 0,
          "search": {
            "method": "GET",
            "url": "https://lyrics.example/search",
            "responseFormat": "JSON",
            "resultPath": "items",
            "mapping": {
              "id": "id",
              "title": "title",
              "artists": "artists"
            }
          },
          "lyrics": {
            "steps": [
              {
                "method": "GET",
                "url": "https://lyrics.example/item/{candidate.id}",
                "responseFormat": "JSON",
                "payloadPath": "lyrics",
                "format": "TEXT"
              }
            ]
          }
        }
    """.trimIndent()
}
