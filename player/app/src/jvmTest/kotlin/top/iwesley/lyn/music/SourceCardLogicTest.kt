package top.iwesley.lyn.music

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import top.iwesley.lyn.music.core.model.ImportScanFailure
import top.iwesley.lyn.music.core.model.ImportScanSummary

class SourceCardLogicTest {
    @Test
    fun `scan summary presentation is absent when summary is missing`() {
        val presentation = buildSourceScanSummaryPresentation(
            summary = null,
            canShowFailures = true,
        )

        assertNull(presentation)
    }

    @Test
    fun `scan summary presentation keeps failure action hidden when no failures exist`() {
        val presentation = buildSourceScanSummaryPresentation(
            summary = ImportScanSummary(
                sourceId = "nav-1",
                discoveredAudioFileCount = 3,
                importedTrackCount = 3,
            ),
            canShowFailures = true,
        )

        requireNotNull(presentation)
        assertEquals("发现 3 个音频文件，成功导入 3 首，0 个失败", presentation.summaryText)
        assertFalse(presentation.showFailuresButton)
    }

    @Test
    fun `scan summary presentation shows failure action for navidrome failures`() {
        val presentation = buildSourceScanSummaryPresentation(
            summary = ImportScanSummary(
                sourceId = "nav-1",
                discoveredAudioFileCount = 3,
                importedTrackCount = 2,
                failures = listOf(
                    ImportScanFailure(
                        relativePath = "Artist A/Album A/Bad.ogg",
                        reason = "当前平台暂不支持导入该音频格式。",
                    ),
                ),
            ),
            canShowFailures = true,
        )

        requireNotNull(presentation)
        assertEquals("发现 3 个音频文件，成功导入 2 首，1 个失败", presentation.summaryText)
        assertTrue(presentation.showFailuresButton)
        assertEquals(listOf("Artist A/Album A/Bad.ogg"), presentation.summary.failures.map { it.relativePath })
    }

    @Test
    fun `scan summary presentation keeps failure action hidden without handler`() {
        val presentation = buildSourceScanSummaryPresentation(
            summary = ImportScanSummary(
                sourceId = "nav-1",
                discoveredAudioFileCount = 2,
                importedTrackCount = 1,
                failures = listOf(
                    ImportScanFailure(
                        relativePath = "Artist A/Album A/Bad.ogg",
                        reason = "当前平台暂不支持导入该音频格式。",
                    ),
                ),
            ),
            canShowFailures = false,
        )

        requireNotNull(presentation)
        assertFalse(presentation.showFailuresButton)
    }
}
