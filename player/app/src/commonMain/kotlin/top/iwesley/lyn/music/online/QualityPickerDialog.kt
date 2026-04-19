package top.iwesley.lyn.music.online

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.iwesley.lyn.music.online.types.Quality

/**
 * 在线歌曲播放音质选择对话框（T11）。
 *
 * 长按搜索结果条触发，覆盖用户的默认音质偏好一次（非持久化）。
 * - [available] 为空时兜底 `K320`，避免无可选项。
 * - 初始选中值随 [current] 重新计算（key 参数），切换到不同 song 时会复位。
 * - 点击 Row 或 RadioButton 均可选中，体验更容易命中。
 */
@Composable
fun QualityPickerDialog(
    available: List<Quality>,
    current: Quality,
    onPick: (Quality) -> Unit,
    onDismiss: () -> Unit,
) {
    val options = available.ifEmpty { listOf(Quality.K320) }
    val initial = if (current in options) current else options.first()
    var selected by remember(current, options) { mutableStateOf(initial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择播放音质") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                options.forEach { quality ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = quality }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = quality == selected,
                            onClick = { selected = quality },
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = quality.displayName,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "${quality.bitrate} kbps · ${quality.lxKey}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onPick(selected)
                onDismiss()
            }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}
