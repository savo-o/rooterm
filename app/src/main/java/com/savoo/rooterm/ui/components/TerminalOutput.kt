package com.savoo.rooterm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.savoo.rooterm.data.OutputLine
import com.savoo.rooterm.data.OutputType
import com.savoo.rooterm.ui.theme.TermColors
import com.savoo.rooterm.ui.theme.TermTheme

@Composable
fun TerminalOutput(
    lines: List<OutputLine>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val tc = TermTheme.colors

    SelectionContainer {
        LazyColumn(
            state   = listState,
            modifier = modifier
                .fillMaxSize()
                .background(tc.background)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            itemsIndexed(
                items = lines,
                key   = { _, line -> line.id },
                contentType = { _, line -> line.type },
            ) { _, line ->
                TermLine(line)
            }
        }
    }
}

private fun TextUnit.lineHeight(factor: Float): TextUnit =
    TextUnit(value * factor, type)

@Composable
private fun TermLine(line: OutputLine) {
    val tc         = TermTheme.colors
    val fontSize   = TermTheme.fontSize
    val fontFamily = TermTheme.fontFamily
    val lh14       = fontSize.lineHeight(1.4f)
    val lh15       = fontSize.lineHeight(1.5f)

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
                    .background(tc.accent.copy(alpha = 0.10f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text       = annotated,
                    fontSize   = fontSize,
                    fontFamily = fontFamily,
                    lineHeight = lh15,
                )
            }
        }

        OutputType.INFO -> {
            Row(
                modifier          = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    Modifier
                        .width(2.dp).height(14.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(tc.accentSecondary)
                )
                Text(
                    text       = line.text,
                    fontSize   = fontSize,
                    fontFamily = fontFamily,
                    color      = tc.accentSecondary,
                    fontStyle  = FontStyle.Italic,
                )
            }
        }

        OutputType.STDERR, OutputType.ERROR -> {
            Text(
                text      = line.text,
                fontSize  = fontSize,
                fontFamily = fontFamily,
                color     = tc.errorColor,
                modifier  = Modifier.padding(vertical = 1.dp),
                lineHeight = lh14,
            )
        }

        OutputType.STDOUT -> {
            val annotated = remember(line.id, tc) { buildStdout(line.text, tc) }
            Text(
                text       = annotated,
                fontSize   = fontSize,
                fontFamily = fontFamily,
                modifier   = Modifier.padding(vertical = 0.5.dp),
                lineHeight = lh14,
            )
        }
    }
}

private val PATH_REGEX  = Regex("""(/[\w./\-_]+)""")
private val NUM_REGEX   = Regex("""\b(\d+)\b""")
private val PERM_REGEX  = Regex("""([rwx\-]{9,10})""")
private const val REGEX_LIMIT = 200

private fun buildStdout(text: String, tc: TermColors): AnnotatedString {
    if (text.length > REGEX_LIMIT) {
        return buildAnnotatedString {
            withStyle(SpanStyle(color = tc.foreground)) { append(text) }
        }
    }

    data class Span(val range: IntRange, val color: androidx.compose.ui.graphics.Color, val weight: FontWeight = FontWeight.Normal)

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
