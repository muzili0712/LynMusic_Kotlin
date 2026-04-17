package top.iwesley.lyn.music

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import top.iwesley.lyn.music.domain.EnhancedLyricsDisplayLine

@Composable
internal fun EnhancedLyricsLineText(
    line: EnhancedLyricsDisplayLine,
    currentPositionMs: Long,
    activeColor: Color,
    inactiveColor: Color,
    style: TextStyle,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    fontWeight: FontWeight? = null,
) {
    val annotatedString = remember(
        line,
        currentPositionMs,
        activeColor,
        inactiveColor,
    ) {
        buildEnhancedLyricsAnnotatedString(
            line = line,
            currentPositionMs = currentPositionMs,
            activeColor = activeColor,
            inactiveColor = inactiveColor,
        )
    }
    Text(
        text = annotatedString,
        modifier = modifier,
        style = style,
        color = activeColor,
        textAlign = textAlign,
        fontWeight = fontWeight,
    )
}

internal fun buildEnhancedLyricsAnnotatedString(
    line: EnhancedLyricsDisplayLine,
    currentPositionMs: Long,
    activeColor: Color,
    inactiveColor: Color,
): AnnotatedString {
    if (line.segments.isEmpty()) {
        return AnnotatedString(line.text)
    }
    val segmentFillFractions = resolveEnhancedLyricsSegmentFillFractions(
        line = line,
        currentPositionMs = currentPositionMs,
    )
    return buildAnnotatedString {
        line.segments.forEachIndexed { index, segment ->
            val start = length
            append(segment.text)
            val end = length
            addStyle(
                style = SpanStyle(
                    color = lerp(
                        start = inactiveColor,
                        stop = activeColor,
                        fraction = segmentFillFractions.getOrElse(index) { 0f },
                    ),
                ),
                start = start,
                end = end,
            )
        }
    }
}

internal fun resolveEnhancedLyricsSegmentFillFractions(
    line: EnhancedLyricsDisplayLine,
    currentPositionMs: Long,
): List<Float> {
    return line.segments.map { segment ->
        val endTimeMs = segment.endTimeMs
        when {
            currentPositionMs < segment.startTimeMs -> 0f
            endTimeMs != null && endTimeMs > segment.startTimeMs -> {
                ((currentPositionMs - segment.startTimeMs).toFloat() /
                    (endTimeMs - segment.startTimeMs).toFloat()).coerceIn(0f, 1f)
            }

            else -> 1f
        }
    }
}
