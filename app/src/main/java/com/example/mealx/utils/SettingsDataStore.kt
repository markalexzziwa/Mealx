package com.example.mealx.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsDataStore {
    private val THEME_DARK_MODE = booleanPreferencesKey("theme_dark_mode")
    private val THEME_FOLLOW_SYSTEM = booleanPreferencesKey("theme_follow_system")

    suspend fun setDarkMode(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[THEME_DARK_MODE] = enabled
        }
    }

    suspend fun setFollowSystemTheme(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[THEME_FOLLOW_SYSTEM] = enabled
        }
    }

    fun getDarkMode(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[THEME_DARK_MODE] ?: false
        }
    }

    fun getFollowSystemTheme(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[THEME_FOLLOW_SYSTEM] ?: true
        }
    }
}