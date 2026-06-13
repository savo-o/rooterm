package com.savoo.rooterm.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.savoo.rooterm.ui.theme.TermTheme

@Composable
fun CommandInput(
    onSend: (String) -> Unit,
    onStop: () -> Unit,
    isRunning: Boolean,
    history: List<String>,
    onFocused: () -> Unit = {},
    searchMode: Boolean = false,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onSearchToggle: () -> Unit = {},
    onSearchNavigate: (Boolean) -> Unit = {},
    matchCount: Int = 0,
    matchIndex: Int = -1,
    modifier: Modifier = Modifier,
) {
    var text         by remember { mutableStateOf("") }
    var historyIdx   by remember { mutableIntStateOf(-1) }
    val focusReq     = remember { FocusRequester() }
    val tc           = TermTheme.colors
    val hasText      = if (searchMode) searchQuery.isNotBlank() else text.isNotBlank()
    var suppressFocus by remember { mutableStateOf(false) }

    LaunchedEffect(searchMode) {
        suppressFocus = true
        suppressFocus = false
    }

    val btnScale by animateFloatAsState(
        targetValue   = if (hasText || isRunning) 1f else 0.85f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "btnScale",
    )
    val btnCorner by animateDpAsState(
        targetValue   = if (hasText || isRunning) 16.dp else 28.dp,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "btnCorner",
    )
    val containerCorner by animateDpAsState(
        targetValue   = if (hasText) 20.dp else 32.dp,
        animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow),
        label         = "inputCorner",
    )
    val iconScale by animateFloatAsState(
        targetValue   = if (isRunning) 1.2f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label         = "iconScale",
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
            AnimatedContent(
                targetState = searchMode,
                transitionSpec = {
                    (fadeIn() + scaleIn(initialScale = 0.8f)) togetherWith (fadeOut() + scaleOut(targetScale = 0.8f))
                },
                label = "arrows",
            ) { isSearch ->
                if (isSearch) {
                    Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                        IconButton(
                            onClick = { onSearchNavigate(false) },
                            modifier = Modifier.size(22.dp),
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, "Previous", Modifier.size(14.dp),
                                tint = if (matchCount > 0) tc.foreground else tc.dimColor.copy(alpha = 0.25f))
                        }
                        IconButton(
                            onClick = { onSearchNavigate(true) },
                            modifier = Modifier.size(22.dp),
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, "Next", Modifier.size(14.dp),
                                tint = if (matchCount > 0) tc.foreground else tc.dimColor.copy(alpha = 0.25f))
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        MiniHistBtn(icon = Icons.Default.KeyboardArrowUp, onClick = ::histUp, enabled = history.isNotEmpty() && !isRunning)
                        MiniHistBtn(icon = Icons.Default.KeyboardArrowDown, onClick = ::histDown, enabled = historyIdx >= 0 && !isRunning)
                    }
                }
            }

            AnimatedContent(
                targetState = searchMode,
                transitionSpec = {
                    (fadeIn() + scaleIn(initialScale = 0.5f)) togetherWith (fadeOut() + scaleOut(targetScale = 0.5f))
                },
                label = "prefix",
            ) { isSearch ->
                if (isSearch) {
                    Icon(Icons.Default.Search, null, Modifier.size(18.dp), tint = tc.accent)
                } else {
                    Text("#", color = tc.promptColor, fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }

            BasicTextField_Wrapper(
                text          = if (searchMode) searchQuery else text,
                onValueChange = { if (searchMode) onSearchQueryChange(it) else text = it },
                cornerRadius  = containerCorner,
                focusReq      = focusReq,
                onSend        = if (searchMode) { { onSearchNavigate(true) } } else ::send,
                onFocused     = { if (!suppressFocus) onFocused() },
                enabled       = !isRunning || searchMode,
                placeholder   = if (searchMode) "search…" else "command…",
                modifier      = Modifier.weight(1f),
            )

            AnimatedContent(
                targetState = searchMode,
                transitionSpec = {
                    (fadeIn() + slideInHorizontally { it / 2 }) togetherWith (fadeOut() + slideOutHorizontally { -it / 2 })
                },
                label = "action",
            ) { isSearch ->
                if (isSearch) {
                    Box(
                        modifier = Modifier
                            .scale(btnScale)
                            .size(44.dp)
                            .clip(RoundedCornerShape(btnCorner))
                            .background(tc.dimColor.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        IconButton(onClick = { onSearchToggle() }, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Default.Close, "Close search", tint = tc.foreground, modifier = Modifier.size(18.dp))
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .scale(btnScale)
                            .size(44.dp)
                            .clip(RoundedCornerShape(btnCorner))
                            .background(if (isRunning) Color(0xFFCF6679) else if (hasText) tc.accent else tc.dimColor.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        IconButton(
                            onClick  = if (isRunning) onStop else ::send,
                            enabled  = hasText || isRunning,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Box(modifier = Modifier.graphicsLayer(scaleX = iconScale, scaleY = iconScale)) {
                                Icon(
                                    imageVector = if (isRunning) Icons.Default.Close else Icons.AutoMirrored.Filled.Send,
                                    contentDescription = if (isRunning) "Stop" else "Send",
                                    tint = if (isRunning) Color.White else if (hasText) tc.background else tc.foreground.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        if (searchMode && matchCount > 0) {
            Text(
                text = "${matchIndex + 1}/$matchCount",
                color = tc.dimColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 4.dp),
            )
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
    enabled: Boolean = true,
    placeholder: String = "command…",
    modifier: Modifier = Modifier,
) {
    val tc = TermTheme.colors

    TextField(
        value         = text,
        onValueChange = onValueChange,
        modifier      = modifier
            .focusRequester(focusReq)
            .onFocusChanged { if (it.isFocused) onFocused() },
        enabled       = enabled,
        placeholder = {
            Text(placeholder, color = tc.dimColor, fontSize = TermTheme.fontSize, fontFamily = FontFamily.Monospace)
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
            disabledContainerColor   = tc.surface.copy(alpha = 0.5f),
            disabledTextColor        = tc.foreground.copy(alpha = 0.5f),
            disabledPlaceholderColor = tc.dimColor.copy(alpha = 0.5f),
            disabledIndicatorColor   = Color.Transparent,
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
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(22.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp),
            tint = if (enabled) tc.foreground else tc.dimColor.copy(alpha = 0.25f))
    }
}
