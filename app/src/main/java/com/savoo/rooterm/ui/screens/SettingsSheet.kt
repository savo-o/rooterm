package com.savoo.rooterm.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.savoo.rooterm.data.ScrollButtonMode
import com.savoo.rooterm.ui.theme.TermColorTheme
import com.savoo.rooterm.ui.theme.TermTheme
import kotlinx.coroutines.delay

@Composable
fun SettingsSheet(
    currentTheme: TermColorTheme,
    currentFontSize: Float,
    hideToolbar: Boolean,
    doubleTapToolbar: Boolean,
    isDarkMode: Boolean,
    scrollButtonMode: ScrollButtonMode,
    scrollButtonSize: Float,
    scrollButtonTop: Float,
    toolbarBottom: Float,
    hapticEnabled: Boolean,
    requireFingerprint: Boolean,
    blockDangerous: Boolean,
    onThemeChange: (TermColorTheme) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onHideToolbarChange: (Boolean) -> Unit,
    onDoubleTapToolbarChange: (Boolean) -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onScrollButtonChange: (ScrollButtonMode) -> Unit,
    onScrollButtonSizeChange: (Float) -> Unit,
    onScrollButtonTopChange: (Float) -> Unit,
    onToolbarBottomChange: (Float) -> Unit,
    onHapticEnabledChange: (Boolean) -> Unit,
    onRequireFingerprintChange: (Boolean) -> Unit,
    onBlockDangerousChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    fun dismiss() { visible = false }

    BackHandler { dismiss() }

    LaunchedEffect(visible) {
        if (!visible) {
            delay(300)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible  = visible,
        enter    = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit     = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    ) {
        SettingsContent(
            currentTheme = currentTheme,
            currentFontSize = currentFontSize,
            hideToolbar = hideToolbar,
            doubleTapToolbar = doubleTapToolbar,
            isDarkMode = isDarkMode,
            scrollButtonMode = scrollButtonMode,
            scrollButtonSize = scrollButtonSize,
            scrollButtonTop = scrollButtonTop,
            toolbarBottom = toolbarBottom,
            hapticEnabled = hapticEnabled,
            requireFingerprint = requireFingerprint,
            blockDangerous = blockDangerous,
            onThemeChange = onThemeChange,
            onFontSizeChange = onFontSizeChange,
            onHideToolbarChange = onHideToolbarChange,
            onDoubleTapToolbarChange = onDoubleTapToolbarChange,
            onDarkModeChange = onDarkModeChange,
            onScrollButtonChange = onScrollButtonChange,
            onScrollButtonSizeChange = onScrollButtonSizeChange,
            onScrollButtonTopChange = onScrollButtonTopChange,
            onToolbarBottomChange = onToolbarBottomChange,
            onHapticEnabledChange = onHapticEnabledChange,
            onRequireFingerprintChange = onRequireFingerprintChange,
            onBlockDangerousChange = onBlockDangerousChange,
            onDismiss = ::dismiss,
        )
    }
}

@Composable
private fun SettingsContent(
    currentTheme: TermColorTheme,
    currentFontSize: Float,
    hideToolbar: Boolean,
    doubleTapToolbar: Boolean,
    isDarkMode: Boolean,
    scrollButtonMode: ScrollButtonMode,
    scrollButtonSize: Float,
    scrollButtonTop: Float,
    toolbarBottom: Float,
    hapticEnabled: Boolean,
    requireFingerprint: Boolean,
    blockDangerous: Boolean,
    onThemeChange: (TermColorTheme) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onHideToolbarChange: (Boolean) -> Unit,
    onDoubleTapToolbarChange: (Boolean) -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onScrollButtonChange: (ScrollButtonMode) -> Unit,
    onScrollButtonSizeChange: (Float) -> Unit,
    onScrollButtonTopChange: (Float) -> Unit,
    onToolbarBottomChange: (Float) -> Unit,
    onHapticEnabledChange: (Boolean) -> Unit,
    onRequireFingerprintChange: (Boolean) -> Unit,
    onBlockDangerousChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var localHideToolbar by remember { mutableStateOf(hideToolbar) }
    var localDoubleTap by remember { mutableStateOf(doubleTapToolbar) }
    var localDarkMode by remember { mutableStateOf(isDarkMode) }
    var localFontSize by remember { mutableFloatStateOf(currentFontSize) }
    var localTheme by remember { mutableStateOf(currentTheme) }
    var localScrollButton by remember { mutableStateOf(scrollButtonMode) }
    var localScrollButtonSize by remember { mutableFloatStateOf(scrollButtonSize) }
    var localScrollButtonTop by remember { mutableFloatStateOf(scrollButtonTop) }
    var localToolbarBottom by remember { mutableFloatStateOf(toolbarBottom) }
    var localHapticEnabled by remember { mutableStateOf(hapticEnabled) }
    var localRequireFingerprint by remember { mutableStateOf(requireFingerprint) }
    var localBlockDangerous by remember { mutableStateOf(blockDangerous) }

    val tc = TermTheme.colors
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    val switchColors = SwitchDefaults.colors(
        checkedThumbColor     = tc.background,
        checkedTrackColor     = tc.accent,
        uncheckedThumbColor   = tc.dimColor,
        uncheckedTrackColor   = tc.dimColor.copy(alpha = 0.3f),
        checkedIconColor      = tc.background,
        uncheckedIconColor    = tc.dimColor,
    )

    val sliderColors = SliderDefaults.colors(
        thumbColor              = tc.accent,
        activeTrackColor        = tc.accent,
        inactiveTrackColor      = tc.accent.copy(alpha = 0.25f),
        activeTickColor         = tc.background,
        inactiveTickColor       = tc.accent.copy(alpha = 0.5f),
        disabledThumbColor      = tc.dimColor,
        disabledActiveTrackColor = tc.dimColor,
        disabledInactiveTrackColor = tc.dimColor.copy(alpha = 0.12f),
    )

    DisposableEffect(Unit) {
        onDispose {
            onHideToolbarChange(localHideToolbar)
            onDoubleTapToolbarChange(localDoubleTap)
            onDarkModeChange(localDarkMode)
            onScrollButtonChange(localScrollButton)
            onScrollButtonSizeChange(localScrollButtonSize)
            onScrollButtonTopChange(localScrollButtonTop)
            onToolbarBottomChange(localToolbarBottom)
            onHapticEnabledChange(localHapticEnabled)
            onRequireFingerprintChange(localRequireFingerprint)
            onBlockDangerousChange(localBlockDangerous)
            onFontSizeChange(localFontSize)
        }
    }

    fun doHaptic() {
        if (!localHapticEnabled) return
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { focusManager.clearFocus(); onDismiss() },
            ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(tc.surface)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { focusManager.clearFocus() },
                )
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Settings, null, tint = tc.accent)
                Text("Settings", color = tc.foreground, style = MaterialTheme.typography.titleLarge)
            }

            HorizontalDivider(color = tc.dimColor.copy(alpha = 0.3f))

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // === THEMES ===
                CategoryHeader(icon = Icons.Default.Palette, title = "Themes")
                SwitchRow("Dark mode", "Switch light and dark theme", localDarkMode, switchColors) {
                    doHaptic(); localDarkMode = it
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Terminal theme", color = tc.foreground, style = MaterialTheme.typography.bodyLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(TermColorTheme.values().toList()) { t ->
                            AnimatedChip(
                                selected = t == localTheme,
                                label = t.displayName,
                                onClick = { doHaptic(); localTheme = t; onThemeChange(t) },
                            )
                        }
                    }
                }
                SliderRow("Font size", "${localFontSize.toInt()} sp",
                    localFontSize, 10f..22f, 11, sliderColors) {
                    localFontSize = it
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Scroll to bottom button", color = tc.foreground, style = MaterialTheme.typography.bodyLarge)
                    Text("Show button to jump to latest output", color = tc.dimColor, style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScrollButtonMode.entries.forEach { mode ->
                            val label = when (mode) {
                                ScrollButtonMode.AUTO -> "Auto"
                                ScrollButtonMode.ALWAYS -> "Always"
                                ScrollButtonMode.NEVER -> "Never"
                            }
                            AnimatedChip(
                                selected = mode == localScrollButton,
                                label = label,
                                onClick = { doHaptic(); localScrollButton = mode },
                            )
                        }
                    }
                }
                SliderRow("Scroll button size", "${localScrollButtonSize.toInt()} dp",
                    localScrollButtonSize, 28f..64f, 8, sliderColors) {
                    localScrollButtonSize = it
                }
                SliderRow("Scroll button height", "${localScrollButtonTop.toInt()} dp",
                    localScrollButtonTop, 4f..200f, 48, sliderColors) {
                    localScrollButtonTop = it
                }

                HorizontalDivider(color = tc.dimColor.copy(alpha = 0.2f))

                // === TOOLBAR ===
                CategoryHeader(icon = Icons.Default.Info, title = "Toolbar")
                SwitchRow("Wait before hide toolbar", "Toolbar hides after inactivity", localHideToolbar, switchColors) {
                    doHaptic(); localHideToolbar = it
                }
                SwitchRow("Double-tap toolbar", "Tap toolbar area twice to show it", localDoubleTap, switchColors) {
                    doHaptic(); localDoubleTap = it
                }
                SliderRow("Toolbar height", "${localToolbarBottom.toInt()} dp",
                    localToolbarBottom, 40f..160f, 30, sliderColors) {
                    localToolbarBottom = it
                }

                HorizontalDivider(color = tc.dimColor.copy(alpha = 0.2f))

                // === BEHAVIOUR ===
                CategoryHeader(icon = Icons.Default.TouchApp, title = "Behaviour")
                SwitchRow("Haptic feedback", "Vibrate on interaction", localHapticEnabled, switchColors) {
                    localHapticEnabled = it
                    if (it) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                SwitchRow("Block dangerous commands", "Prevent destructive shell commands", localBlockDangerous, switchColors) {
                    localBlockDangerous = it
                }

                HorizontalDivider(color = tc.dimColor.copy(alpha = 0.2f))

                // === ABOUT ===
                val context = androidx.compose.ui.platform.LocalContext.current
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            doHaptic()
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://github.com/savo-o/rooterm")
                            )
                            context.startActivity(intent)
                        }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("About", color = tc.foreground, style = MaterialTheme.typography.bodyLarge)
                        Text("author: savo", color = tc.dimColor, style = MaterialTheme.typography.bodySmall)
                    }
                    Text("v0.5", color = tc.accent, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    val tc = TermTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, null, tint = tc.accent, modifier = Modifier.size(20.dp))
        Text(title, color = tc.foreground, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, colors: SwitchColors, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(title, color = TermTheme.colors.foreground, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, color = TermTheme.colors.dimColor, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = colors)
    }
}

@Composable
private fun SliderRow(label: String, valueText: String, value: Float, range: ClosedFloatingPointRange<Float>, steps: Int, colors: SliderColors, onValueChange: (Float) -> Unit) {
    val tc = TermTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = tc.foreground, style = MaterialTheme.typography.bodyLarge)
            Text(valueText, color = tc.accent, style = MaterialTheme.typography.bodyMedium)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = range, steps = steps, colors = colors)
    }
}

@Composable
private fun AnimatedChip(selected: Boolean, label: String, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = if (pressed) 0.5f else 0.35f,
            stiffness    = if (pressed) 800f else 300f,
        ),
        label = "chipScale",
    )
    val tc = TermTheme.colors

    Box(
        modifier = Modifier
            .scale(scale)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val anyPressed = event.changes.any { it.pressed }
                        pressed = anyPressed
                    }
                }
            },
    ) {
        FilterChip(
            selected = selected,
            onClick  = onClick,
            label    = { Text(label) },
            colors   = FilterChipDefaults.filterChipColors(
                selectedContainerColor = tc.accent,
                selectedLabelColor     = tc.background,
                containerColor         = tc.dimColor.copy(alpha = 0.3f),
                labelColor             = tc.foreground,
            ),
        )
    }
}
