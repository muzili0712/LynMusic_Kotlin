package top.iwesley.lyn.music.online

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.iwesley.lyn.music.online.store.OnlineSearchIntent
import top.iwesley.lyn.music.online.store.OnlineSearchState
import top.iwesley.lyn.music.online.types.OnlineSong
import top.iwesley.lyn.music.online.types.Quality

/**
 * 在线搜索主 UI：源选择栏 + 关键字输入框 + 当前源的结果列表。
 *
 * 输入框每次变动都会派发 [OnlineSearchIntent.QueryChanged]；
 * - 点击结果调用 `onPlay(song, null)`，表示沿用用户默认音质偏好；
 * - 长按结果弹出 [QualityPickerDialog]，选定后调用 `onPlay(song, quality)` 本次播放覆盖默认音质。
 *
 * 加载/错误来自 [OnlineSearchState.loadingBySource] / [OnlineSearchState.errorBySource]，
 * 以当前 [OnlineSearchState.activeSource] 为键读取。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OnlineSearchUi(
    state: OnlineSearchState,
    onIntent: (OnlineSearchIntent) -> Unit,
    onPlay: (OnlineSong, Quality?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var qualityPickFor by remember { mutableStateOf<OnlineSong?>(null) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp),
    ) {
        SourcePickerBar(
            sources = state.availableSources,
            activeSourceId = state.activeSource,
            onSourceSelected = { onIntent(OnlineSearchIntent.SourceSelected(it)) },
        )

        OutlinedTextField(
            value = state.query,
            onValueChange = { onIntent(OnlineSearchIntent.QueryChanged(it)) },
            label = { Text("搜索歌曲 / 歌手") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        val loading = state.loadingBySource[state.activeSource] == true
        val error = state.errorBySource[state.activeSource]
        val page = state.perSourceResults[state.activeSource]
        val items = page?.items.orEmpty()

        when {
            loading && items.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null && items.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "搜索失败：$error",
                        color = MaterialTheme.colorScheme.error,
                    )
                    TextButton(onClick = { onIntent(OnlineSearchIntent.Retry) }) {
                        Text("重试")
                    }
                }
            }

            state.query.isBlank() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "输入关键字开始搜索",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "暂无结果",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items, key = { it.id.stableKey }) { song ->
                        OnlineSongRow(
                            song = song,
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { onPlay(song, null) },
                                    onLongClick = { qualityPickFor = song },
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    qualityPickFor?.let { song ->
        QualityPickerDialog(
            available = song.availableQualities.ifEmpty { listOf(Quality.K320) },
            current = song.defaultQuality,
            onPick = { quality ->
                onPlay(song, quality)
                qualityPickFor = null
            },
            onDismiss = { qualityPickFor = null },
        )
    }
}

@Composable
private fun OnlineSongRow(
    song: OnlineSong,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = song.name,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(2.dp))
        val subtitle = buildString {
            append(song.singer)
            song.album?.takeIf { it.isNotBlank() }?.let {
                append(" · ")
                append(it)
            }
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
