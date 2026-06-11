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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    currentTheme: TermColorTheme,
    currentFontSize: Float,
    dynamicColor: Boolean,
    hideToolbar: Boolean,
    doubleTapToolbar: Boolean,
    onThemeChange: (TermColorTheme) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onDynamicChange: (Boolean) -> Unit,
    onHideToolbarChange: (Boolean) -> Unit,
    onDoubleTapToolbarChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var localDynamic by remember { mutableStateOf(dynamicColor) }
    var localHideToolbar by remember { mutableStateOf(hideToolbar) }
    var localDoubleTap by remember { mutableStateOf(doubleTapToolbar) }
    var localFontSize by remember { mutableFloatStateOf(currentFontSize) }
    var localTheme by remember { mutableStateOf(currentTheme) }

    DisposableEffect(Unit) {
        onDispose {
            onDynamicChange(localDynamic)
            onHideToolbarChange(localHideToolbar)
            onDoubleTapToolbarChange(localDoubleTap)
        }
    }

    ModalBottomSheet(
        onDismissRequest  = onDismiss,
        sheetState        = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        containerColor    = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Palette, null, tint = MaterialTheme.colorScheme.primary)
                Text("Settings", style = MaterialTheme.typography.titleLarge)
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Dynamic color", style = MaterialTheme.typography.bodyLarge)
                    Text("Follow wallpaper accent", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = localDynamic,
                    onCheckedChange = { localDynamic = it }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Hide toolbar", style = MaterialTheme.typography.bodyLarge)
                    Text("Auto-hide floating buttons", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = localHideToolbar,
                    onCheckedChange = { localHideToolbar = it }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Double-tap toolbar", style = MaterialTheme.typography.bodyLarge)
                    Text("Tap toolbar area twice to show it", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = localDoubleTap,
                    onCheckedChange = { localDoubleTap = it }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Terminal theme", style = MaterialTheme.typography.bodyLarge)
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
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor     = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Font size", style = MaterialTheme.typography.bodyLarge)
                    Text("${localFontSize.toInt()} sp",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = localFontSize,
                    onValueChange = { localFontSize = it },
                    onValueChangeFinished = { onFontSizeChange(localFontSize) },
                    valueRange = 10f..22f,
                    steps = 11,
                )
            }
        }
    }
}
