package top.iwesley.lyn.music.feature.settings

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 验证 SettingsStore 对默认音质（Quality.lxKey）偏好的加载与 write-through 行为。
 * 存字符串形式便于跨平台持久化；Store 在初始化时订阅 repository.defaultQualityKey 流。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsDefaultQualityTest {

    @Test
    fun `store exposes default quality from repository on load`() = runTest {
        val repository = FakeSettingsRepository(defaultQualityKey = "flac")
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler) + SupervisorJob())
        val store = SettingsStore(repository, scope)

        advanceUntilIdle()

        assertEquals("flac", store.state.value.defaultQualityLxKey)
        scope.cancel()
    }

    @Test
    fun `dispatching DefaultQualityChanged persists and updates state`() = runTest {
        val repository = FakeSettingsRepository(defaultQualityKey = "128k")
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler) + SupervisorJob())
        val store = SettingsStore(repository, scope)

        advanceUntilIdle()
        assertEquals("128k", store.state.value.defaultQualityLxKey)

        store.dispatch(SettingsIntent.DefaultQualityChanged("flac"))
        advanceUntilIdle()

        assertEquals("flac", store.state.value.defaultQualityLxKey)
        assertEquals("flac", repository.currentDefaultQualityKey())
        scope.cancel()
    }
}
