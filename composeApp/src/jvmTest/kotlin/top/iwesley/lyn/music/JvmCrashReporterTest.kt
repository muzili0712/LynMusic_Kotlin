package top.iwesley.lyn.music

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

class JvmCrashReporterTest {
    @Test
    fun `formats crash report with thread exception message and cause`() {
        val throwable = IllegalStateException(
            "outer failure",
            IllegalArgumentException("inner failure"),
        )

        val report = formatJvmCrashReport(
            threadName = "player-worker",
            throwable = throwable,
        )

        assertContains(report, "LynMusic Desktop Crash")
        assertContains(report, "Thread: player-worker")
        assertContains(report, "Exception: java.lang.IllegalStateException")
        assertContains(report, "Message: outer failure")
        assertContains(report, "Caused by: java.lang.IllegalArgumentException: inner failure")
    }

    @Test
    fun `truncates oversized crash report`() {
        val throwable = IllegalStateException("large failure").apply {
            stackTrace = Array(2_000) { index ->
                StackTraceElement(
                    "top.iwesley.lyn.music.CrashReporterVeryLongClassName$index",
                    "methodWithALongName$index",
                    "CrashReporterVeryLongFileName.kt",
                    index + 1,
                )
            }
        }

        val report = formatJvmCrashReport(
            threadName = "overflow-thread",
            throwable = throwable,
            maxChars = 800,
        )

        assertTrue(report.length <= 800)
        assertContains(report, "Thread: overflow-thread")
        assertContains(report, "[crash report truncated]")
    }
}
