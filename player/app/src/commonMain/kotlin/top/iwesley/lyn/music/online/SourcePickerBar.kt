package top.iwesley.lyn.music.online

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.iwesley.lyn.music.online.types.SourceInfo

/**
 * 在线搜索顶部的源切换栏：横向可滚动的 FilterChip 列表。
 *
 * - 只渲染 [sources]；上层（Store）已过滤成 enabled 源清单。
 * - 选中态用 [activeSourceId] 命中；点击发回 [onSourceSelected]。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourcePickerBar(
    sources: List<SourceInfo>,
    activeSourceId: String,
    onSourceSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        sources.forEach { source ->
            val selected = source.id == activeSourceId
            FilterChip(
                selected = selected,
                onClick = { onSourceSelected(source.id) },
                label = { Text(source.displayName) },
                colors = FilterChipDefaults.filterChipColors(),
            )
        }
    }
}
