package com.savoo.rooterm.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.savoo.rooterm.TerminalViewModel
import com.savoo.rooterm.ui.components.CommandInput
import com.savoo.rooterm.ui.components.TermTabRow
import com.savoo.rooterm.ui.components.TerminalOutput
import com.savoo.rooterm.ui.theme.TermTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TerminalScreen(vm: TerminalViewModel) {
    val sessions  = vm.sessions
    val activeIdx = vm.activeIndex.value
    val session   = sessions.getOrNull(activeIdx)

    val termTheme = vm.termTheme.collectAsState()
    val fontSize  = vm.fontSize.collectAsState()
    val dynColor  = vm.dynamicColor.collectAsState()

    val listState = rememberLazyListState()
    val scope     = rememberCoroutineScope()
    val tc        = TermTheme.colors

    var showSettings by remember { mutableStateOf(false) }

    var toolbarVisible by remember { mutableStateOf(true) }
    var lastFirstVisible by remember { mutableIntStateOf(0) }

    LaunchedEffect(listState.firstVisibleItemIndex) {
        val current = listState.firstVisibleItemIndex
        toolbarVisible = current <= lastFirstVisible 
        lastFirstVisible = current
    }

    LaunchedEffect(session?.output?.size) {
        val size = session?.output?.size ?: 0
        if (size > 0) scope.launch {
            listState.animateScrollToItem(size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Column(Modifier.fillMaxSize()) {
            TermTabRow(
                sessions    = sessions,
                activeIndex = activeIdx,
                onSelect    = vm::selectTab,
                onNew       = vm::newTab,
                onClose     = vm::closeTab,
            )

            Box(modifier = Modifier.weight(1f)) {
                if (session != null) {
                    TerminalOutput(
                        lines     = session.output,
                        listState = listState,
                        modifier  = Modifier.fillMaxSize(),
                    )
                }
            }

            CommandInput(
                onSend  = vm::sendCommand,
                history = vm.commandHistory,
            )
        }

        AnimatedVisibility(
            visible  = toolbarVisible,
            enter    = fadeIn() + slideInHorizontally { it },
            exit     = fadeOut() + slideOutHorizontally { it },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 100.dp),
        ) {
            HorizontalFloatingToolbar(
                expanded = true,
                floatingActionButton = {
                    FloatingToolbarDefaults.VibrantFloatingActionButton(
                        onClick        = vm::newTab,
                        containerColor = tc.accent,
                    ) {
                        Icon(Icons.Default.Add, "New tab")
                    }
                },
                colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
            ) {
                IconButton(onClick = vm::clearCurrent) {
                    Icon(Icons.Default.DeleteSweep, "Clear")
                }
                IconButton(onClick = { showSettings = true }) {
                    Icon(Icons.Default.Settings, "Settings")
                }
            }
        }
    }

    if (showSettings) {
        SettingsSheet(
            currentTheme     = termTheme.value,
            currentFontSize  = fontSize.value,
            dynamicColor     = dynColor.value,
            onThemeChange    = vm::setTheme,
            onFontSizeChange = vm::setFontSize,
            onDynamicChange  = vm::setDynamic,
            onDismiss        = { showSettings = false },
        )
    }
}
