package top.iwesley.lyn.music.online

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import top.iwesley.lyn.music.core.model.DiagnosticLogLevel
import top.iwesley.lyn.music.core.model.DiagnosticLogger
import top.iwesley.lyn.music.core.model.GlobalDiagnosticLogger
import top.iwesley.lyn.music.online.diagnostics.OnlineLogTags
import top.iwesley.lyn.music.online.repository.OnlineMusicRepository

/**
 * 验证 [OnlineMusicRepository] 在 search 命中缓存时会走 [GlobalDiagnosticLogger]
 * 吐一条 `MSRC.cache` 的 "hit ..." 日志。
 *
 * 通过 [GlobalDiagnosticLogger.installStrategy] 把 logger 临时替换成内存捕获实现，
 * 测试完毕用 [GlobalDiagnosticLogger.resetStrategy] 恢复。
 */
class DiagnosticsLoggingTest {

    private data class CapturedEntry(
        val level: DiagnosticLogLevel,
        val tag: String,
        val message: String,
    )

    private class CapturingLogger : DiagnosticLogger {
        val entries: MutableList<CapturedEntry> = mutableListOf()
        override fun log(
            level: DiagnosticLogLevel,
            tag: String,
            message: String,
            throwable: Throwable?,
        ) {
            entries += CapturedEntry(level, tag, message)
        }
    }

    private lateinit var capturing: CapturingLogger

    @BeforeTest
    fun installCapturingLogger() {
        capturing = CapturingLogger()
        GlobalDiagnosticLogger.installStrategy(capturing)
    }

    @AfterTest
    fun resetLogger() {
        GlobalDiagnosticLogger.resetStrategy()
    }

    @Test
    fun repository_search_cache_hit_emits_log() = runTest {
        val facade = FakeMusicSourceFacade()
        val repo = OnlineMusicRepository(facade)

        // 第一次：miss → 打 "miss ..."
        repo.search("kw", "jay", page = 1, limit = 30)
        // 第二次：hit → 打 "hit ..."
        repo.search("kw", "jay", page = 1, limit = 30)

        val cacheLogs = capturing.entries.filter { it.tag == OnlineLogTags.CACHE }
        assertTrue(cacheLogs.any { it.message.startsWith("miss ") }, "expected a miss log, got: $cacheLogs")
        assertTrue(cacheLogs.any { it.message.startsWith("hit ") }, "expected a hit log, got: $cacheLogs")
    }
}
