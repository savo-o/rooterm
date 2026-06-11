package com.savoo.rooterm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.savoo.rooterm.ui.theme.TermColorTheme
import com.savoo.rooterm.ui.theme.TermTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    currentTheme: TermColorTheme,
    currentFontSize: Float,
    hideToolbar: Boolean,
    doubleTapToolbar: Boolean,
    isDarkMode: Boolean,
    onThemeChange: (TermColorTheme) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onHideToolbarChange: (Boolean) -> Unit,
    onDoubleTapToolbarChange: (Boolean) -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var localHideToolbar by remember { mutableStateOf(hideToolbar) }
    var localDoubleTap by remember { mutableStateOf(doubleTapToolbar) }
    var localDarkMode by remember { mutableStateOf(isDarkMode) }
    var localFontSize by remember { mutableFloatStateOf(currentFontSize) }
    var localTheme by remember { mutableStateOf(currentTheme) }

    val tc = TermTheme.colors

    val switchColors = SwitchDefaults.colors(
        checkedThumbColor   = tc.background,
        checkedTrackColor   = tc.accent,
        uncheckedThumbColor = tc.dimColor,
        uncheckedTrackColor = tc.dimColor.copy(alpha = 0.3f),
    )

    val sliderColors = SliderDefaults.colors(
        thumbColor       = tc.accent,
        activeTrackColor = tc.accent,
        inactiveTrackColor = tc.dimColor.copy(alpha = 0.3f),
    )

    DisposableEffect(Unit) {
        onDispose {
            onHideToolbarChange(localHideToolbar)
            onDoubleTapToolbarChange(localDoubleTap)
            onDarkModeChange(localDarkMode)
        }
    }

    ModalBottomSheet(
        onDismissRequest  = onDismiss,
        sheetState        = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        containerColor    = tc.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Palette, null, tint = tc.accent)
                Text("Settings", color = tc.foreground, style = MaterialTheme.typography.titleLarge)
            }

            HorizontalDivider(color = tc.dimColor.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Dark mode", color = tc.foreground, style = MaterialTheme.typography.bodyLarge)
                    Text("Switch light and dark theme", color = tc.dimColor, style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = localDarkMode,
                    onCheckedChange = { localDarkMode = it },
                    colors = switchColors,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Wait before hide toolbar", color = tc.foreground, style = MaterialTheme.typography.bodyLarge)
                    Text("Toolbar hides after inactivity", color = tc.dimColor, style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = localHideToolbar,
                    onCheckedChange = { localHideToolbar = it },
                    colors = switchColors,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Double-tap toolbar", color = tc.foreground, style = MaterialTheme.typography.bodyLarge)
                    Text("Tap toolbar area twice to show it", color = tc.dimColor, style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = localDoubleTap,
                    onCheckedChange = { localDoubleTap = it },
                    colors = switchColors,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Terminal theme", color = tc.foreground, style = MaterialTheme.typography.bodyLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TermColorTheme.values().toList()) { t ->
                        FilterChip(
                            selected = t == localTheme,
                            onClick  = {
                                localTheme = t
                                onThemeChange(t)
                            },
                            label    = { Text(t.displayName) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = tc.accent,
                                selectedLabelColor     = tc.background,
                            ),
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Font size", color = tc.foreground, style = MaterialTheme.typography.bodyLarge)
                    Text("${localFontSize.toInt()} sp", color = tc.accent, style = MaterialTheme.typography.bodyMedium)
                }
                Slider(
                    value = localFontSize,
                    onValueChange = { localFontSize = it },
                    onValueChangeFinished = { onFontSizeChange(localFontSize) },
                    valueRange = 10f..22f,
                    steps = 11,
                    colors = sliderColors,
                )
            }
        }
    }
}
