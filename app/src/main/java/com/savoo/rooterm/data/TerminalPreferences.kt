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
    val THEME        = stringPreferencesKey("term_theme")
    val FONT_SIZE    = floatPreferencesKey("font_size")
    val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
}

class TerminalPreferences(private val ctx: Context) {
    val theme: Flow<TermColorTheme> = ctx.dataStore.data.map { p ->
        runCatching { TermColorTheme.valueOf(p[PrefKeys.THEME] ?: "") }
            .getOrDefault(TermColorTheme.MATERIAL_MONO)
    }
    val fontSize: Flow<Float>   = ctx.dataStore.data.map { it[PrefKeys.FONT_SIZE] ?: 14f }
    val dynamic: Flow<Boolean>  = ctx.dataStore.data.map { it[PrefKeys.DYNAMIC_COLOR] ?: true }

    suspend fun setTheme(t: TermColorTheme) = ctx.dataStore.edit { it[PrefKeys.THEME] = t.name }
    suspend fun setFontSize(s: Float)       = ctx.dataStore.edit { it[PrefKeys.FONT_SIZE] = s }
    suspend fun setDynamic(b: Boolean)      = ctx.dataStore.edit { it[PrefKeys.DYNAMIC_COLOR] = b }
}
