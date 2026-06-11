package com.savoo.rooterm.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.savoo.rooterm.TerminalViewModel
import com.savoo.rooterm.ui.components.CommandInput
import com.savoo.rooterm.ui.components.TermTabRow
import com.savoo.rooterm.ui.components.TerminalOutput
import com.savoo.rooterm.ui.theme.TermTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TerminalScreen(vm: TerminalViewModel) {
    val sessions         = vm.sessions
    val activeIdx        = vm.activeIndex.value
    val session          = sessions.getOrNull(activeIdx)
    val hideToolbar      = vm.hideToolbar.collectAsState()
    val doubleTapEnabled = vm.doubleTapToolbar.collectAsState()
    val isDarkMode       = vm.darkMode.collectAsState()

    val termTheme = vm.termTheme.collectAsState()
    val fontSize  = vm.fontSize.collectAsState()

    val listState = rememberLazyListState()
    val tc        = TermTheme.colors
    val scope     = rememberCoroutineScope()

    var showSettings by remember { mutableStateOf(false) }
    var toolbarVisible by remember { mutableStateOf(false) }
    var lastFirstVisible by remember { mutableIntStateOf(0) }
    var hideJob by remember { mutableStateOf<Job?>(null) }

    fun atBottom(): Boolean {
        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        val total = listState.layoutInfo.totalItemsCount
        return total == 0 || lastVisible >= total - 3
    }

    fun showToolbarFor(durationMs: Long) {
        toolbarVisible = true
        hideJob?.cancel()
        hideJob = scope.launch {
            delay(durationMs)
            toolbarVisible = false
        }
    }

    fun scheduleHide() {
        showToolbarFor(1250)
    }

    LaunchedEffect(Unit) {
        showToolbarFor(1000)
    }

    LaunchedEffect(listState.isScrollInProgress) {
        session?.isScrolling = listState.isScrollInProgress
        if (!listState.isScrollInProgress) {
            if (hideToolbar.value) {
                scheduleHide()
            } else {
                if (atBottom()) toolbarVisible = false
            }
        }
    }

    LaunchedEffect(listState.firstVisibleItemIndex) {
        if (listState.isScrollInProgress) {
            val current = listState.firstVisibleItemIndex
            val scrollingUp = current < lastFirstVisible
            val scrollingDown = current > lastFirstVisible

            if (scrollingUp) {
                toolbarVisible = true
                hideJob?.cancel()
            }
            if (scrollingDown && !hideToolbar.value) {
                if (atBottom()) toolbarVisible = false
            }

            lastFirstVisible = current
        }
    }

    LaunchedEffect(session?.id) {
        sessions.forEach { it.isScrolling = false }
        toolbarVisible = false
        lastFirstVisible = 0
    }

    LaunchedEffect(session?.id) {
        snapshotFlow { session?.output?.size ?: 0 }
            .distinctUntilChanged()
            .collect { size ->
                if (size > 0) {
                    val force = session?.scrollToBottom == true
                    if (force) session?.scrollToBottom = false
                    if (force || atBottom()) {
                        listState.scrollToItem(size - 1)
                        toolbarVisible = false
                    }
                }
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

        if (doubleTapEnabled.value) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 0.dp, bottom = 96.dp)
                    .size(width = 110.dp, height = 110.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = { showToolbarFor(2000) }
                        )
                    },
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
                        contentColor   = tc.background,
                    ) {
                        Icon(Icons.Default.Add, "New tab")
                    }
                },
            ) {
                IconButton(onClick = vm::clearCurrent) {
                    Icon(Icons.Default.DeleteSweep, "Clear", tint = tc.foreground)
                }
                IconButton(onClick = { showSettings = true }) {
                    Icon(Icons.Default.Settings, "Settings", tint = tc.foreground)
                }
            }
        }
    }

    if (showSettings) {
        SettingsSheet(
            currentTheme           = termTheme.value,
            currentFontSize        = fontSize.value,
            hideToolbar            = hideToolbar.value,
            doubleTapToolbar       = doubleTapEnabled.value,
            isDarkMode             = isDarkMode.value,
            onThemeChange          = vm::setTheme,
            onFontSizeChange       = vm::setFontSize,
            onHideToolbarChange    = vm::setHideToolbar,
            onDoubleTapToolbarChange = vm::setDoubleTapToolbar,
            onDarkModeChange       = vm::setDarkMode,
            onDismiss              = { showSettings = false },
        )
    }
}
