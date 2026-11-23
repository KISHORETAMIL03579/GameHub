package com.gamehub.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "gamehub_settings")

class PreferencesManager(private val context: Context) {
    companion object {
        val KEY_CURRENCY = stringPreferencesKey("preferred_currency")
        val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
        val KEY_STEAM_USER = stringPreferencesKey("steam_user")
        val KEY_EPIC_USER = stringPreferencesKey("epic_user")
        val KEY_API_BASE_URL = stringPreferencesKey("api_base_url")
    }

    val currencyFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CURRENCY] ?: "INR"
    }

    val darkThemeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DARK_THEME] ?: true
    }

    val steamUserFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_STEAM_USER]
    }

    val epicUserFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_EPIC_USER]
    }

    val apiBaseUrlFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_API_BASE_URL] ?: "http://127.0.0.1:8000/"
    }

    suspend fun setCurrency(currency: String) {
        context.dataStore.edit { it[KEY_CURRENCY] = currency }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK_THEME] = enabled }
    }

    suspend fun setSteamUser(user: String?) {
        context.dataStore.edit {
            if (user != null) it[KEY_STEAM_USER] = user else it.remove(KEY_STEAM_USER)
        }
    }

    suspend fun setEpicUser(user: String?) {
        context.dataStore.edit {
            if (user != null) it[KEY_EPIC_USER] = user else it.remove(KEY_EPIC_USER)
        }
    }

    suspend fun setApiBaseUrl(url: String) {
        context.dataStore.edit { it[KEY_API_BASE_URL] = url }
    }
}
