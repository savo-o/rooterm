package com.savoo.rooterm.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.savoo.rooterm.data.OutputLine
import com.savoo.rooterm.data.OutputType
import com.savoo.rooterm.ui.theme.TermColors
import com.savoo.rooterm.ui.theme.TermTheme

@Composable
fun TerminalOutput(
    lines: List<OutputLine>,
    listState: LazyListState,
    searchQuery: String = "",
    currentMatchIndex: Int = -1,
    searchMatches: List<Int> = emptyList(),
    modifier: Modifier = Modifier,
) {
    val tc = TermTheme.colors
    val ctx = LocalContext.current

    LazyColumn(
        state   = listState,
        modifier = modifier
            .fillMaxSize()
            .background(tc.background)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { offset ->
                        val item = listState.layoutInfo.visibleItemsInfo.firstOrNull { info ->
                            offset.y.toInt() in info.offset..(info.offset + info.size)
                        }
                        if (item != null) {
                            val line = lines.getOrNull(item.index)
                            if (line != null) {
                                val clip = ClipData.newPlainText("terminal", line.text)
                                ctx.getSystemService(ClipboardManager::class.java)?.setPrimaryClip(clip)
                                Toast.makeText(ctx, "Copied", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                )
            },
    ) {
        itemsIndexed(
            items = lines,
            key   = { _, line -> line.id },
            contentType = { _, line -> line.type },
        ) { index, line ->
            val isCurrentMatch = searchMatches.isNotEmpty() &&
                currentMatchIndex >= 0 &&
                searchMatches.getOrNull(currentMatchIndex) == index
            TermLine(
                line = line,
                searchQuery = searchQuery,
                isHighlighted = isCurrentMatch,
            )
        }
    }
}

private fun TextUnit.lineHeight(factor: Float): TextUnit =
    TextUnit(value * factor, type)

@Composable
private fun TermLine(line: OutputLine, searchQuery: String = "", isHighlighted: Boolean = false) {
    val tc         = TermTheme.colors
    val fontSize   = TermTheme.fontSize
    val fontFamily = TermTheme.fontFamily
    val lh14       = fontSize.lineHeight(1.4f)
    val lh15       = fontSize.lineHeight(1.5f)

    val highlightBg = if (isHighlighted) tc.accent.copy(alpha = 0.25f) else null

    when (line.type) {
        OutputType.COMMAND -> {
            val annotated = remember(line.id, tc) {
                buildAnnotatedString {
                    withStyle(SpanStyle(color = tc.promptColor, fontWeight = FontWeight.Bold)) { append("# ") }
                    withStyle(SpanStyle(color = tc.accent, fontWeight = FontWeight.SemiBold)) {
                        append(line.text.removePrefix("# "))
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(highlightBg ?: tc.accent.copy(alpha = 0.10f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(text = annotated, fontSize = fontSize, fontFamily = fontFamily, lineHeight = lh15)
            }
        }

        OutputType.INFO -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp)
                    .then(if (highlightBg != null) Modifier.background(highlightBg) else Modifier),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    Modifier
                        .width(2.dp).height(14.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(tc.accentSecondary)
                )
                val infoText = buildSearchText(line.text, searchQuery, tc.foreground, tc.accent)
                Text(text = infoText, fontSize = fontSize, fontFamily = fontFamily,
                    color = tc.accentSecondary, fontStyle = FontStyle.Italic)
            }
        }

        OutputType.STDERR, OutputType.ERROR -> {
            val errText = buildSearchText(line.text, searchQuery, tc.errorColor, tc.accent)
            Text(text = errText, fontSize = fontSize, fontFamily = fontFamily,
                color = tc.errorColor, modifier = Modifier
                    .padding(vertical = 1.dp)
                    .then(if (highlightBg != null) Modifier.background(highlightBg) else Modifier),
                lineHeight = lh14)
        }

        OutputType.STDOUT -> {
            val annotated = remember(line.id, tc) { buildStdout(line.text, tc) }
            val finalText = if (searchQuery.isNotBlank()) {
                buildSearchAnnotated(line.text, searchQuery, tc)
            } else {
                annotated
            }
            Text(text = finalText, fontSize = fontSize, fontFamily = fontFamily,
                modifier = Modifier
                    .padding(vertical = 0.5.dp)
                    .then(if (highlightBg != null) Modifier.background(highlightBg) else Modifier),
                lineHeight = lh14)
        }
    }
}

private fun buildSearchText(text: String, query: String, baseColor: androidx.compose.ui.graphics.Color, highlightColor: androidx.compose.ui.graphics.Color): AnnotatedString {
    if (query.isBlank()) return buildAnnotatedString { withStyle(SpanStyle(color = baseColor)) { append(text) } }

    return buildAnnotatedString {
        var cursor = 0
        val q = query.lowercase()
        val t = text.lowercase()
        while (cursor < text.length) {
            val idx = t.indexOf(q, cursor)
            if (idx < 0) {
                withStyle(SpanStyle(color = baseColor)) { append(text.substring(cursor)) }
                break
            }
            if (idx > cursor) {
                withStyle(SpanStyle(color = baseColor)) { append(text.substring(cursor, idx)) }
            }
            withStyle(SpanStyle(color = highlightColor, background = highlightColor.copy(alpha = 0.3f), fontWeight = FontWeight.Bold)) {
                append(text.substring(idx, idx + query.length))
            }
            cursor = idx + query.length
        }
    }
}

private fun buildSearchAnnotated(text: String, query: String, tc: TermColors): AnnotatedString {
    val base = buildStdout(text, tc)
    if (query.isBlank()) return base

    return buildAnnotatedString {
        var cursor = 0
        val q = query.lowercase()
        val t = text.lowercase()
        while (cursor < text.length) {
            val idx = t.indexOf(q, cursor)
            if (idx < 0) {
                append(base.subSequence(cursor, text.length))
                break
            }
            if (idx > cursor) {
                append(base.subSequence(cursor, idx))
            }
            withStyle(SpanStyle(background = tc.accent.copy(alpha = 0.3f), fontWeight = FontWeight.Bold)) {
                append(base.subSequence(idx, idx + query.length))
            }
            cursor = idx + query.length
        }
    }
}

private val PATH_REGEX  = Regex("""(/[\w./\-_]+)""")
private val NUM_REGEX   = Regex("""\b(\d+)\b""")
private val PERM_REGEX  = Regex("""([rwx\-]{9,10})""")
private const val REGEX_LIMIT = 200

private class Span(val range: IntRange, val color: androidx.compose.ui.graphics.Color, val weight: FontWeight = FontWeight.Normal)

private fun buildStdout(text: String, tc: TermColors): AnnotatedString {
    if (text.length > REGEX_LIMIT) {
        return buildAnnotatedString {
            withStyle(SpanStyle(color = tc.foreground)) { append(text) }
        }
    }

    val spans = mutableListOf<Span>()
    PATH_REGEX.findAll(text).forEach { spans.add(Span(it.range, tc.accentSecondary, FontWeight.Medium)) }
    PERM_REGEX.findAll(text).forEach { spans.add(Span(it.range, tc.accent.copy(alpha = 0.85f))) }
    NUM_REGEX.findAll(text).forEach  { m ->
        if (spans.none { m.range.first in it.range }) spans.add(Span(m.range, tc.dimColor.copy(alpha = 0.8f)))
    }
    spans.sortBy { it.range.first }

    return buildAnnotatedString {
        var cursor = 0
        for (span in spans) {
            if (span.range.first < cursor) continue
            withStyle(SpanStyle(color = tc.foreground)) { append(text.substring(cursor, span.range.first)) }
            withStyle(SpanStyle(color = span.color, fontWeight = span.weight)) { append(text.substring(span.range)) }
            cursor = span.range.last + 1
        }
        if (cursor < text.length) withStyle(SpanStyle(color = tc.foreground)) { append(text.substring(cursor)) }
    }
}
