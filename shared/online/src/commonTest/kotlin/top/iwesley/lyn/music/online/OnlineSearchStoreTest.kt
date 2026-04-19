package top.iwesley.lyn.music.online

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import top.iwesley.lyn.music.online.repository.OnlineMusicRepository
import top.iwesley.lyn.music.online.store.OnlineSearchIntent
import top.iwesley.lyn.music.online.store.OnlineSearchState
import top.iwesley.lyn.music.online.store.OnlineSearchStore

/**
 * [OnlineSearchStore] 契约测试：initial state / QueryChanged 触发搜索 / SourceSelected 重新触发。
 *
 * 使用 [FakeMusicSourceFacade]（T6 commonTest 已有），通过 [FakeMusicSourceFacade.searchCallCount]
 * 确认搜索是否真正触达底层 facade（而非仅命中缓存或完全未触发）。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OnlineSearchStoreTest {

    @Test
    fun initial_state_contains_enabled_sources_and_empty_results() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(dispatcher)
        val store = OnlineSearchStore(
            repository = OnlineMusicRepository(FakeMusicSourceFacade()),
            scope = scope,
        )

        val snapshot = store.state.value
        assertEquals("", snapshot.query)
        assertEquals(
            listOf("kw", "kg", "tx", "wy", "mg"),
            snapshot.availableSources.map { it.id },
            "M0 应默认加载 SourceManifest.enabled 的 5 源",
        )
        assertTrue(snapshot.perSourceResults.isEmpty())
        assertTrue(snapshot.loadingBySource.isEmpty())
        assertTrue(snapshot.errorBySource.isEmpty())
        assertEquals("kw", snapshot.activeSource, "activeSource 默认选 kw（首个启用源）")
    }

    @Test
    fun query_changed_triggers_search_on_active_source() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(dispatcher)
        val fake = FakeMusicSourceFacade()
        val store = OnlineSearchStore(
            repository = OnlineMusicRepository(fake),
            scope = scope,
        )

        store.dispatch(OnlineSearchIntent.QueryChanged("jay"))
        advanceUntilIdle()

        assertEquals(1, fake.searchCallCount, "QueryChanged 应对活动源触发一次搜索")
        val state = store.state.value
        assertEquals("jay", state.query)
        val page = state.perSourceResults["kw"]
        assertNotNull(page, "活动源 kw 应有结果")
        assertEquals(1, page.items.size)
        assertEquals(false, state.loadingBySource["kw"], "loading 结束后应落为 false")
        assertTrue(state.errorBySource["kw"] == null, "成功后不应有错误")
    }

    @Test
    fun source_selected_retriggers_search_on_new_source_when_query_nonempty() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(dispatcher)
        val fake = FakeMusicSourceFacade()
        val store = OnlineSearchStore(
            repository = OnlineMusicRepository(fake),
            scope = scope,
        )

        store.dispatch(OnlineSearchIntent.QueryChanged("周杰伦"))
        advanceUntilIdle()
        assertEquals(1, fake.searchCallCount)

        // 切到 wy，应再次触发 facade.search（新 source key 的缓存 miss）
        store.dispatch(OnlineSearchIntent.SourceSelected("wy"))
        advanceUntilIdle()

        assertEquals(2, fake.searchCallCount, "切源 + 非空关键字应再触发一次搜索")
        val state = store.state.value
        assertEquals("wy", state.activeSource)
        assertNotNull(state.perSourceResults["kw"], "kw 的旧结果应仍保留")
        assertNotNull(state.perSourceResults["wy"], "wy 的新结果应写入")
    }
}
