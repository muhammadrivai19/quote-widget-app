package com.quotewidget.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.prefs by preferencesDataStore("settings")

object SettingsStore {
    private val INTERVAL = intPreferencesKey("intervalMinutes")
    private val ORDER = stringPreferencesKey("orderMode")
    private val ONBOARDING = booleanPreferencesKey("onboardingDone")
    private val THEME = stringPreferencesKey("themeId")
    private val TEXT_COLOR = stringPreferencesKey("textColorHex")
    private val FONT = stringPreferencesKey("fontId")
    private val SWIPE_HINT = booleanPreferencesKey("swipeHintShown")

    const val SEQUENTIAL = "sequential"
    const val RANDOM = "random"

    val INTERVALS = listOf(15, 30, 60, 180, 360, 720, 1440)

    fun intervalFlow(c: Context): Flow<Int> =
        c.prefs.data.map { it[INTERVAL] ?: 60 }

    fun orderFlow(c: Context): Flow<String> =
        c.prefs.data.map { it[ORDER] ?: SEQUENTIAL }

    fun onboardingFlow(c: Context): Flow<Boolean> =
        c.prefs.data.map { it[ONBOARDING] ?: false }

    fun themeFlow(c: Context): Flow<String> =
        c.prefs.data.map { it[THEME] ?: "kertas" }

    suspend fun theme(c: Context): String =
        c.prefs.data.map { it[THEME] ?: "kertas" }.first()

    suspend fun setTheme(c: Context, v: String) {
        c.prefs.edit { it[THEME] = v }
    }

    suspend fun interval(c: Context): Int =
        c.prefs.data.map { it[INTERVAL] ?: 60 }.first()

    suspend fun order(c: Context): String =
        c.prefs.data.map { it[ORDER] ?: SEQUENTIAL }.first()

    suspend fun setInterval(c: Context, v: Int) {
        c.prefs.edit { it[INTERVAL] = v.coerceAtLeast(15) }
    }

    suspend fun setOrder(c: Context, v: String) {
        c.prefs.edit { it[ORDER] = v }
    }

    suspend fun setOnboardingDone(c: Context) {
        c.prefs.edit { it[ONBOARDING] = true }
    }

    fun textColorFlow(c: Context): Flow<String?> =
        c.prefs.data.map { it[TEXT_COLOR] }

    suspend fun textColor(c: Context): String? =
        c.prefs.data.map { it[TEXT_COLOR] }.first()

    suspend fun setTextColor(c: Context, v: String?) {
        c.prefs.edit {
            if (v == null) it.remove(TEXT_COLOR) else it[TEXT_COLOR] = v
        }
    }

    fun fontFlow(c: Context): Flow<String> =
        c.prefs.data.map { it[FONT] ?: "serif" }

    suspend fun font(c: Context): String =
        c.prefs.data.map { it[FONT] ?: "serif" }.first()

    suspend fun setFont(c: Context, v: String) {
        c.prefs.edit { it[FONT] = v }
    }

    fun swipeHintShownFlow(c: Context): Flow<Boolean> =
        c.prefs.data.map { it[SWIPE_HINT] ?: false }

    suspend fun setSwipeHintShown(c: Context) {
        c.prefs.edit { it[SWIPE_HINT] = true }
    }
}
