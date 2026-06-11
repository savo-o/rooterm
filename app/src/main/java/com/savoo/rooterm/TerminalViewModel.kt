package com.savoo.rooterm

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.savoo.rooterm.data.TerminalPreferences
import com.savoo.rooterm.data.TerminalSession
import com.savoo.rooterm.ui.theme.TermColorTheme
import com.savoo.rooterm.util.SuRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TerminalViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = TerminalPreferences(app)

    val sessions      = mutableStateListOf<TerminalSession>()
    val activeIndex   = mutableStateOf(0)

    val termTheme    = prefs.theme.stateIn(viewModelScope, SharingStarted.Eagerly, TermColorTheme.MATERIAL_MONO)
    val fontSize     = prefs.fontSize.stateIn(viewModelScope, SharingStarted.Eagerly, 14f)
    val hideToolbar  = prefs.hideToolbar.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val doubleTapToolbar = prefs.doubleTapToolbar.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val darkMode = prefs.darkMode.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val commandHistory = mutableStateListOf<String>()

    init {
        newTab()
        viewModelScope.launch(Dispatchers.Main) {
            while (isActive) {
                delay(50)
                for (s in sessions) s.flushPending()
            }
        }
    }

    fun newTab() {
        val s = TerminalSession(title = "tab ${sessions.size + 1}")
        sessions.add(s)
        activeIndex.value = sessions.lastIndex
        viewModelScope.launch(Dispatchers.IO) { SuRunner.initSession(s) }
    }

    fun closeTab(i: Int) {
        if (sessions.size <= 1) return
        SuRunner.kill(sessions[i])
        sessions.removeAt(i)
        activeIndex.value = minOf(activeIndex.value, sessions.lastIndex)
    }

    fun selectTab(i: Int) { activeIndex.value = i }

    fun sendCommand(cmd: String) {
        if (cmd.isBlank()) return
        commandHistory.remove(cmd)
        commandHistory.add(cmd)
        val s = sessions.getOrNull(activeIndex.value) ?: return
        viewModelScope.launch(Dispatchers.IO) { SuRunner.send(s, cmd) }
    }

    fun clearCurrent() { sessions.getOrNull(activeIndex.value)?.clear() }

    fun sendInterrupt() {
        val s = sessions.getOrNull(activeIndex.value) ?: return
        viewModelScope.launch(Dispatchers.IO) { SuRunner.sendInterrupt(s) }
    }

    fun setTheme(t: TermColorTheme)     = viewModelScope.launch { prefs.setTheme(t) }
    fun setFontSize(f: Float)           = viewModelScope.launch { prefs.setFontSize(f) }
    fun setHideToolbar(b: Boolean)      = viewModelScope.launch { prefs.setHideToolbar(b) }
    fun setDoubleTapToolbar(b: Boolean) = viewModelScope.launch { prefs.setDoubleTapToolbar(b) }
    fun setDarkMode(b: Boolean)         = viewModelScope.launch { prefs.setDarkMode(b) }

    override fun onCleared() {
        super.onCleared()
        sessions.forEach { SuRunner.kill(it) }
    }
}
