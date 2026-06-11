package com.savoo.rooterm.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.savoo.rooterm.ui.theme.TermColorTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rooterm_prefs")

object PrefKeys {
    val THEME              = stringPreferencesKey("term_theme")
    val FONT_SIZE          = floatPreferencesKey("font_size")
    val HIDE_TOOLBAR       = booleanPreferencesKey("hide_toolbar")
    val DOUBLE_TAP_TOOLBAR = booleanPreferencesKey("double_tap_toolbar")
    val DARK_MODE          = booleanPreferencesKey("dark_mode")
}

class TerminalPreferences(private val ctx: Context) {
    val theme: Flow<TermColorTheme> = ctx.dataStore.data.map { p ->
        runCatching { TermColorTheme.valueOf(p[PrefKeys.THEME] ?: "") }
            .getOrDefault(TermColorTheme.MATERIAL_MONO)
    }
    val fontSize: Flow<Float>   = ctx.dataStore.data.map { it[PrefKeys.FONT_SIZE] ?: 14f }
    val hideToolbar: Flow<Boolean> = ctx.dataStore.data.map { it[PrefKeys.HIDE_TOOLBAR] ?: true }
    val doubleTapToolbar: Flow<Boolean> = ctx.dataStore.data.map { it[PrefKeys.DOUBLE_TAP_TOOLBAR] ?: true }
    val darkMode: Flow<Boolean> = ctx.dataStore.data.map { it[PrefKeys.DARK_MODE] ?: true }

    suspend fun setTheme(t: TermColorTheme) = ctx.dataStore.edit { it[PrefKeys.THEME] = t.name }
    suspend fun setFontSize(s: Float)       = ctx.dataStore.edit { it[PrefKeys.FONT_SIZE] = s }
    suspend fun setHideToolbar(b: Boolean)  = ctx.dataStore.edit { it[PrefKeys.HIDE_TOOLBAR] = b }
    suspend fun setDoubleTapToolbar(b: Boolean) = ctx.dataStore.edit { it[PrefKeys.DOUBLE_TAP_TOOLBAR] = b }
    suspend fun setDarkMode(b: Boolean)     = ctx.dataStore.edit { it[PrefKeys.DARK_MODE] = b }
}
