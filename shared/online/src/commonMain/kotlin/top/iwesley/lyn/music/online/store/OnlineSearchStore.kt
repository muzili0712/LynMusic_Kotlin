package top.iwesley.lyn.music.online.store

import kotlinx.coroutines.CoroutineScope
import top.iwesley.lyn.music.core.mvi.BaseStore
import top.iwesley.lyn.music.online.repository.OnlineMusicRepository
import top.iwesley.lyn.music.online.types.OnlineSong
import top.iwesley.lyn.music.online.types.SearchPage
import top.iwesley.lyn.music.online.types.SourceInfo
import top.iwesley.lyn.music.online.types.SourceManifest

/**
 * M0 在线搜索 MVI Store。
 *
 * 状态：
 *  - [OnlineSearchState.query] 当前关键字；非空时才触发搜索。
 *  - [OnlineSearchState.activeSource] 用户选中的源（UI 展示时按此键读 [perSourceResults]）。
 *  - [OnlineSearchState.availableSources] UI 渲染源选择栏；M0 默认取 [SourceManifest.enabled]（5 源）。
 *  - [OnlineSearchState.perSourceResults] 每源最近一次成功搜索的分页结果。
 *  - [OnlineSearchState.loadingBySource] 每源当前是否正在请求（独立于其他源，便于并发展示）。
 *  - [OnlineSearchState.errorBySource] 每源最近一次失败的错误消息；成功后会清空。
 *
 * Intent 设计遵循"输入即触发"风格：关键字变动 → 对活动源立刻跑一次搜索；
 * 换源 → 若关键字非空则跑新源的搜索。Retry 用于失败后的手动重试（活动源 + 当前关键字）。
 *
 * 不在 T7 范围：分页（loadMore）、多源聚合并发查询、搜索建议 —— 交给 M1。
 */
class OnlineSearchStore(
    private val repository: OnlineMusicRepository,
    scope: CoroutineScope,
    initialState: OnlineSearchState = OnlineSearchState(),
) : BaseStore<OnlineSearchState, OnlineSearchIntent, OnlineSearchEffect>(
    initialState = initialState,
    scope = scope,
) {

    override suspend fun handleIntent(intent: OnlineSearchIntent) {
        when (intent) {
            is OnlineSearchIntent.QueryChanged -> {
                updateState { it.copy(query = intent.query) }
                runSearch(state.value.activeSource, intent.query)
            }

            is OnlineSearchIntent.SourceSelected -> {
                updateState { it.copy(activeSource = intent.sourceId) }
                val q = state.value.query
                if (q.isNotBlank()) {
                    runSearch(intent.sourceId, q)
                }
            }

            OnlineSearchIntent.Retry -> {
                val snapshot = state.value
                if (snapshot.query.isNotBlank()) {
                    runSearch(snapshot.activeSource, snapshot.query)
                }
            }
        }
    }

    private suspend fun runSearch(sourceId: String, query: String) {
        if (query.isBlank()) return
        updateState {
            it.copy(
                loadingBySource = it.loadingBySource + (sourceId to true),
                errorBySource = it.errorBySource - sourceId,
            )
        }
        try {
            val page = repository.search(sourceId, query)
            updateState {
                it.copy(
                    perSourceResults = it.perSourceResults + (sourceId to page),
                    loadingBySource = it.loadingBySource + (sourceId to false),
                    errorBySource = it.errorBySource - sourceId,
                )
            }
        } catch (t: Throwable) {
            val message = t.message ?: t::class.simpleName ?: "unknown error"
            updateState {
                it.copy(
                    loadingBySource = it.loadingBySource + (sourceId to false),
                    errorBySource = it.errorBySource + (sourceId to message),
                )
            }
            emitEffect(OnlineSearchEffect.ShowError(sourceId, message))
        }
    }
}

data class OnlineSearchState(
    val query: String = "",
    val activeSource: String = SourceManifest.enabled.first().id,
    val availableSources: List<SourceInfo> = SourceManifest.enabled,
    val perSourceResults: Map<String, SearchPage<OnlineSong>> = emptyMap(),
    val loadingBySource: Map<String, Boolean> = emptyMap(),
    val errorBySource: Map<String, String> = emptyMap(),
)

sealed interface OnlineSearchIntent {
    data class QueryChanged(val query: String) : OnlineSearchIntent
    data class SourceSelected(val sourceId: String) : OnlineSearchIntent
    data object Retry : OnlineSearchIntent
}

sealed interface OnlineSearchEffect {
    data class ShowError(val sourceId: String, val message: String) : OnlineSearchEffect
}
