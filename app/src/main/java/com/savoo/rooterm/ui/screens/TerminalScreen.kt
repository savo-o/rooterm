package com.savoo.rooterm.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.savoo.rooterm.TerminalViewModel
import com.savoo.rooterm.data.ScrollButtonMode
import com.savoo.rooterm.ui.components.CommandInput
import com.savoo.rooterm.ui.components.TermTabRow
import com.savoo.rooterm.ui.components.TerminalOutput
import com.savoo.rooterm.ui.theme.TermTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private class CloverShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val r = size.width * 0.30f
        val cx = size.width / 2f
        val cy = size.height / 2f
        val d = r * 0.75f
        val path = Path()
        fun addPetal(px: Float, py: Float) {
            path.addOval(Rect(px - r, py - r, px + r, py + r))
        }
        addPetal(cx - d, cy - d)
        addPetal(cx + d, cy - d)
        addPetal(cx - d, cy + d)
        addPetal(cx + d, cy + d)
        addPetal(cx, cy)
        return Outline.Generic(path)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TerminalScreen(vm: TerminalViewModel) {
    val sessions         = vm.sessions
    val activeIdx        = vm.activeIndex.value
    val session          = sessions.getOrNull(activeIdx)
    val hideToolbar      = vm.hideToolbar.collectAsState()
    val doubleTapEnabled = vm.doubleTapToolbar.collectAsState()
    val isDarkMode       = vm.darkMode.collectAsState()
    val scrollButtonMode = vm.scrollButton.collectAsState()
    val scrollButtonSize = vm.scrollButtonSize.collectAsState()
    val scrollButtonTop = vm.scrollButtonTop.collectAsState()
    val toolbarBottom = vm.toolbarBottom.collectAsState()
    val hapticEnabled = vm.hapticEnabled.collectAsState()
    val haptic = LocalHapticFeedback.current

    val termTheme = vm.termTheme.collectAsState()
    val fontSize  = vm.fontSize.collectAsState()

    val listState = rememberLazyListState()
    val tc        = TermTheme.colors
    val scope     = rememberCoroutineScope()

    var showSettings by remember { mutableStateOf(false) }
    var toolbarVisible by remember { mutableStateOf(false) }
    var lastFirstVisible by remember { mutableIntStateOf(0) }
    var hideJob by remember { mutableStateOf<Job?>(null) }

    val lastVisibleIndex by remember {
        derivedStateOf { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
    }
    val totalItems by remember {
        derivedStateOf { listState.layoutInfo.totalItemsCount }
    }
    val atBottom by remember {
        derivedStateOf { totalItems == 0 || lastVisibleIndex >= totalItems - 10 }
    }

    fun scrollToBottom() {
        session?.autoScroll = true
        toolbarVisible = false
        scope.launch {
            try {
                val size = session?.output?.size ?: 0
                if (size > 0) listState.scrollToItem(size - 1)
            } catch (_: Exception) {}
        }
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
                if (atBottom) toolbarVisible = false
            }
        }
    }

    LaunchedEffect(listState.firstVisibleItemIndex) {
        if (listState.isScrollInProgress) {
            val current = listState.firstVisibleItemIndex
            if (current < lastFirstVisible) {
                session?.autoScroll = false
                toolbarVisible = true
                hideJob?.cancel()
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
            .collectLatest { size ->
                if (size > 0 && session?.autoScroll == true) {
                    try {
                        listState.scrollToItem(size - 1)
                        toolbarVisible = false
                    } catch (_: Exception) {}
                }
            }
    }

    LaunchedEffect(session?.autoScroll) {
        if (session?.autoScroll == true) {
            try {
                val size = session.output.size
                if (size > 0) listState.scrollToItem(size - 1)
            } catch (_: Exception) {}
        }
    }

    val showScrollButton = remember(scrollButtonMode.value, session?.autoScroll, atBottom) {
        when (scrollButtonMode.value) {
            ScrollButtonMode.ALWAYS -> true
            ScrollButtonMode.AUTO -> !atBottom
            ScrollButtonMode.NEVER -> false
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
                onSend    = vm::sendCommand,
                history   = vm.commandHistory,
                onFocused = { scrollToBottom() },
            )
        }

        if (doubleTapEnabled.value) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 0.dp, bottom = toolbarBottom.value.dp - 4.dp)
                    .size(width = 110.dp, height = 110.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = { showToolbarFor(2000) }
                        )
                    },
            )
        }

        AnimatedVisibility(
            visible  = showScrollButton,
            enter    = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit     = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = scrollButtonTop.value.dp),
        ) {
            Box(
                modifier = Modifier.padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                FloatingActionButton(
                    onClick        = { if (hapticEnabled.value) haptic.performHapticFeedback(HapticFeedbackType.LongPress); scrollToBottom() },
                    containerColor = tc.accent,
                    contentColor   = tc.background,
                    shape          = CloverShape(),
                    elevation      = FloatingActionButtonDefaults.elevation(4.dp),
                    modifier       = Modifier.size(scrollButtonSize.value.dp),
                ) {
                    Icon(
                        Icons.Default.ArrowDownward,
                        contentDescription = "Scroll to bottom",
                        modifier = Modifier.size((scrollButtonSize.value * 0.4f).dp),
                    )
                }
            }
        }

        AnimatedVisibility(
            visible  = toolbarVisible,
            enter    = fadeIn() + slideInHorizontally { it },
            exit     = fadeOut() + slideOutHorizontally { it },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = toolbarBottom.value.dp),
        ) {
            Box(modifier = Modifier.padding(8.dp)) {
                HorizontalFloatingToolbar(
                    expanded = true,
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick        = { if (hapticEnabled.value) haptic.performHapticFeedback(HapticFeedbackType.LongPress); vm.newTab() },
                            containerColor = tc.accent,
                            contentColor   = tc.background,
                            shape          = CloverShape(),
                            elevation      = FloatingActionButtonDefaults.elevation(0.dp),
                        ) {
                            Icon(Icons.Default.Add, "New tab")
                        }
                    },
                ) {
                    IconButton(onClick = { if (hapticEnabled.value) haptic.performHapticFeedback(HapticFeedbackType.LongPress); vm.clearCurrent() }) {
                        Icon(Icons.Default.DeleteSweep, "Clear", tint = tc.foreground)
                    }
                    IconButton(onClick = { if (hapticEnabled.value) haptic.performHapticFeedback(HapticFeedbackType.LongPress); showSettings = true }) {
                        Icon(Icons.Default.Settings, "Settings", tint = tc.foreground)
                    }
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
            scrollButtonMode       = scrollButtonMode.value,
            scrollButtonSize       = scrollButtonSize.value,
            scrollButtonTop        = scrollButtonTop.value,
            toolbarBottom          = toolbarBottom.value,
            hapticEnabled          = hapticEnabled.value,
            onThemeChange          = vm::setTheme,
            onFontSizeChange       = vm::setFontSize,
            onHideToolbarChange    = vm::setHideToolbar,
            onDoubleTapToolbarChange = vm::setDoubleTapToolbar,
            onDarkModeChange       = vm::setDarkMode,
            onScrollButtonChange   = vm::setScrollButton,
            onScrollButtonSizeChange = vm::setScrollButtonSize,
            onScrollButtonTopChange  = vm::setScrollButtonTop,
            onToolbarBottomChange   = vm::setToolbarBottom,
            onHapticEnabledChange  = vm::setHapticEnabled,
            onDismiss              = { showSettings = false },
        )
    }
}
