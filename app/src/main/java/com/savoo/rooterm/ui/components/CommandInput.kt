package com.savoo.rooterm.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.savoo.rooterm.ui.theme.TermTheme

@Composable
fun CommandInput(
    onSend: (String) -> Unit,
    history: List<String>,
    onFocused: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var text         by remember { mutableStateOf("") }
    var historyIdx   by remember { mutableIntStateOf(-1) }
    val focusReq     = remember { FocusRequester() }
    val tc           = TermTheme.colors
    val hasText      = text.isNotBlank()
    var hadFocus     by remember { mutableStateOf(false) }

    val btnScale by animateFloatAsState(
        targetValue   = if (hasText) 1f else 0.85f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "btnScale",
    )
    val btnCorner by animateDpAsState(
        targetValue   = if (hasText) 16.dp else 28.dp,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "btnCorner",
    )
    val containerCorner by animateDpAsState(
        targetValue   = if (hasText) 20.dp else 32.dp,
        animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow),
        label         = "inputCorner",
    )

    fun send() {
        val cmd = text.trim()
        if (cmd.isNotEmpty()) { onSend(cmd); historyIdx = -1; text = "" }
    }
    fun histUp() {
        if (history.isEmpty()) return
        historyIdx = minOf(historyIdx + 1, history.lastIndex)
        text = history[history.size - 1 - historyIdx]
    }
    fun histDown() {
        if (historyIdx <= 0) { historyIdx = -1; text = "" }
        else { historyIdx--; text = history[history.size - 1 - historyIdx] }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(tc.background)
            .padding(horizontal = 12.dp, vertical = 17.dp),
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                MiniHistBtn(icon = Icons.Default.KeyboardArrowUp,   onClick = ::histUp,   enabled = history.isNotEmpty())
                MiniHistBtn(icon = Icons.Default.KeyboardArrowDown,  onClick = ::histDown, enabled = historyIdx >= 0)
            }

            Text(
                text       = "#",
                color      = tc.promptColor,
                fontSize   = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            )

            BasicTextField_Wrapper(
                text          = text,
                onValueChange = { text = it },
                cornerRadius  = containerCorner,
                focusReq      = focusReq,
                onSend        = ::send,
                onFocused     = onFocused,
                modifier      = Modifier.weight(1f),
            )

            Box(
                modifier = Modifier
                    .scale(btnScale)
                    .size(44.dp)
                    .clip(RoundedCornerShape(btnCorner))
                    .background(if (hasText) tc.accent else tc.dimColor.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(
                    onClick  = ::send,
                    enabled  = hasText,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint     = if (hasText) tc.background else tc.foreground.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun BasicTextField_Wrapper(
    text: String,
    onValueChange: (String) -> Unit,
    cornerRadius: androidx.compose.ui.unit.Dp,
    focusReq: FocusRequester,
    onSend: () -> Unit,
    onFocused: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val tc = TermTheme.colors

    TextField(
        value         = text,
        onValueChange = onValueChange,
        modifier      = modifier
            .focusRequester(focusReq)
            .onFocusChanged { if (it.isFocused) onFocused() },
        placeholder = {
            Text(
                "command…",
                color      = tc.dimColor,
                fontSize   = TermTheme.fontSize,
                fontFamily = FontFamily.Monospace,
            )
        },
        singleLine  = true,
        textStyle   = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace,
            color      = tc.foreground,
            fontSize   = TermTheme.fontSize,
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor    = tc.surface,
            unfocusedContainerColor  = tc.surface.copy(alpha = 0.7f),
            focusedIndicatorColor    = Color.Transparent,
            unfocusedIndicatorColor  = Color.Transparent,
            cursorColor              = tc.accent,
        ),
        shape = RoundedCornerShape(cornerRadius),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = { onSend() }),
    )
}

@Composable
private fun MiniHistBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    val tc = TermTheme.colors
    IconButton(
        onClick  = onClick,
        enabled  = enabled,
        modifier = Modifier.size(22.dp),
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint     = if (enabled) tc.foreground else tc.dimColor.copy(alpha = 0.25f),
        )
    }
}
