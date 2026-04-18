package top.iwesley.lyn.music.data.db

import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Verifies that the v9 Room schema is exported and declares the new `online_song`
 * table plus the `origin` columns appended to `favorite_track` / `playlist_track`.
 *
 * Room's `MigrationTestHelper` is Android-only, so we validate the exported schema
 * JSON instead. The Gradle test runner's working directory is the module root
 * (`shared/data`), which makes `schemas/` a reliable relative path.
 */
class Migration8To9Test {

    @Test
    fun `v9 schema exports online_song table and origin columns`() = runTest {
        val candidatePaths = listOf(
            "schemas/top.iwesley.lyn.music.data.db.LynMusicDatabase/9.json",
            "../schemas/top.iwesley.lyn.music.data.db.LynMusicDatabase/9.json",
            "shared/data/schemas/top.iwesley.lyn.music.data.db.LynMusicDatabase/9.json",
        )
        val file = candidatePaths
            .map { File(it) }
            .firstOrNull { it.exists() }
            ?: error(
                "v9 schema not found in any of: $candidatePaths. " +
                    "Ensure KSP ran for a target that exports Room schemas.",
            )

        val json = file.readText()
        assertTrue(
            json.contains("\"tableName\": \"online_song\""),
            "online_song table missing in v9 schema at ${file.absolutePath}",
        )
        // Every column appears as a JSON object; at least one must be named `origin`.
        assertTrue(
            json.contains("\"columnName\": \"origin\""),
            "origin column missing in v9 schema at ${file.absolutePath}",
        )
        assertTrue(
            json.contains("\"version\": 9"),
            "schema version is not 9 at ${file.absolutePath}",
        )
    }
}
