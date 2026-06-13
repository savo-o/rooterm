package com.savoo.rooterm.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
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

    val termTheme = vm.termTheme.collectAsState()
    val fontSize  = vm.fontSize.collectAsState()

    val listState = rememberLazyListState()
    val tc        = TermTheme.colors
    val scope     = rememberCoroutineScope()

    var showSettings by remember { mutableStateOf(false) }
    var toolbarVisible by remember { mutableStateOf(false) }
    var lastFirstVisible by remember { mutableIntStateOf(0) }
    var hideJob by remember { mutableStateOf<Job?>(null) }

    var searchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchMatches by remember { mutableStateOf(listOf<Int>()) }
    var currentMatchIndex by remember { mutableIntStateOf(-1) }

    val lastVisibleIndex by remember {
        derivedStateOf { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
    }
    val totalItems by remember {
        derivedStateOf { listState.layoutInfo.totalItemsCount }
    }
    val atBottom by remember {
        derivedStateOf { totalItems == 0 || lastVisibleIndex >= totalItems - 10 }
    }

    val haptic = LocalHapticFeedback.current

    fun doHaptic() {
        if (hapticEnabled.value) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    fun scrollToBottom() {
        toolbarVisible = false
        session?.scrollNowTrigger?.intValue = (session?.scrollNowTrigger?.intValue ?: 0) + 1
    }

    fun computeSearchMatches() {
        if (searchQuery.isBlank() || session == null) {
            searchMatches = emptyList()
            currentMatchIndex = -1
            return
        }
        val query = searchQuery.lowercase()
        val matches = mutableListOf<Int>()
        session.output.forEachIndexed { index, line ->
            if (line.text.lowercase().contains(query)) {
                matches.add(index)
            }
        }
        searchMatches = matches
        if (matches.isNotEmpty()) {
            currentMatchIndex = 0
            scope.launch {
                try { listState.animateScrollToItem(matches[0]) } catch (_: Exception) {}
            }
        } else {
            currentMatchIndex = -1
        }
    }

    fun navigateMatch(forward: Boolean) {
        if (searchMatches.isEmpty()) return
        currentMatchIndex = if (forward) {
            (currentMatchIndex + 1).coerceAtMost(searchMatches.lastIndex)
        } else {
            (currentMatchIndex - 1).coerceAtLeast(0)
        }
        scope.launch {
            try { listState.animateScrollToItem(searchMatches[currentMatchIndex]) } catch (_: Exception) {}
        }
        doHaptic()
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
        searchActive = false
        searchQuery = ""
        searchMatches = emptyList()
        currentMatchIndex = -1
    }

    LaunchedEffect(session?.scrollNowTrigger?.intValue) {
        if (session?.scrollNowTrigger?.intValue != 0) {
            val size = session?.output?.size ?: 0
            if (size > 0) {
                try { listState.animateScrollToItem(size - 1) } catch (_: Exception) {}
            }
        }
    }

    LaunchedEffect(session?.suppressOutput?.value) {
        if (session?.suppressOutput?.value == true) {
            snapshotFlow { session?.output?.size ?: 0 }
                .collect { size ->
                    if (size > 0) {
                        try { listState.scrollToItem(size - 1) } catch (_: Exception) {}
                    }
                }
        }
    }

    LaunchedEffect(session?.id) {
        snapshotFlow { session?.output?.size ?: 0 }
            .distinctUntilChanged()
            .collect { size ->
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

    LaunchedEffect(searchQuery) {
        computeSearchMatches()
    }

    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val isKeyboardVisible by remember {
        derivedStateOf { imeInsets.getBottom(density) > 0 }
    }

    BackHandler(searchActive) {
        if (isKeyboardVisible) {
            focusManager.clearFocus()
        } else {
            searchActive = false
            searchQuery = ""
            searchMatches = emptyList()
            currentMatchIndex = -1
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
                        lines            = session.output,
                        listState        = listState,
                        searchQuery      = searchQuery,
                        currentMatchIndex = currentMatchIndex,
                        searchMatches    = searchMatches,
                        modifier         = Modifier.fillMaxSize(),
                    )
                }
            }

            CommandInput(
                onSend         = vm::sendCommand,
                onStop         = {
                    session?.suppressOutput?.value = true
                    session?.autoScroll = false
                    session?.scrollNowTrigger?.intValue = (session?.scrollNowTrigger?.intValue ?: 0) + 1
                    vm.stopCommand()
                },
                isRunning      = session?.isCommandRunning?.value == true,
                history        = vm.commandHistory,
                onFocused      = { scrollToBottom() },
                searchMode     = searchActive,
                searchQuery    = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearchToggle = { searchActive = !searchActive; if (!searchActive) { searchQuery = ""; searchMatches = emptyList(); currentMatchIndex = -1 } },
                onSearchNavigate = ::navigateMatch,
                matchCount     = searchMatches.size,
                matchIndex     = currentMatchIndex,
            )
        }

        if (doubleTapEnabled.value) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 0.dp, bottom = toolbarBottom.value.dp - 4.dp)
                    .size(width = 180.dp, height = 140.dp)
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
                    onClick        = { doHaptic(); scrollToBottom() },
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
                            onClick        = { doHaptic(); vm.newTab() },
                            containerColor = tc.accent,
                            contentColor   = tc.background,
                            shape          = CloverShape(),
                            elevation      = FloatingActionButtonDefaults.elevation(0.dp),
                        ) {
                            Icon(Icons.Default.Add, "New tab")
                        }
                    },
                ) {
                    IconButton(onClick = { doHaptic(); showToolbarFor(2000); vm.clearCurrent() }) {
                        Icon(Icons.Default.DeleteSweep, "Clear", tint = tc.foreground)
                    }
                    IconButton(onClick = { doHaptic(); showToolbarFor(2000); searchActive = !searchActive; if (!searchActive) { searchQuery = ""; searchMatches = emptyList(); currentMatchIndex = -1 } }) {
                        Icon(Icons.Default.Search, "Search", tint = if (searchActive) tc.accent else tc.foreground)
                    }
                    IconButton(onClick = { doHaptic(); showToolbarFor(2000); showSettings = true; focusManager.clearFocus() }) {
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
            requireFingerprint     = vm.requireFingerprint.collectAsState().value,
            blockDangerous         = vm.blockDangerous.collectAsState().value,
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
            onRequireFingerprintChange = vm::setRequireFingerprint,
            onBlockDangerousChange  = vm::setBlockDangerous,
            onDismiss              = { showSettings = false },
        )
    }
}

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
