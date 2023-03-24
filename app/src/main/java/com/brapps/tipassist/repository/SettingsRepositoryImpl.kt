package com.brapps.tipassist.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.brapps.tipassist.models.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "userSettings")

class SettingsRepositoryImpl(private val context: Context) : SettingsRepository {
    companion object{
        private const val DEFAULT_CURRENCY = "$"
        private const val DEFAULT_NIGHT_MODE = "System Default"
        private val CURRENCY = stringPreferencesKey("NAME")
        private val THEME_SELECTED = stringPreferencesKey("THEME")
        private const val TAG = "SettingsRepositoryImpl"
    }

    override suspend fun saveSettings(settings: UserSettings) {
        context.dataStore.edit { preferences->
            preferences[CURRENCY] = settings.currency
            preferences[THEME_SELECTED] = settings.theme
        }
        Log.i(TAG, "currencyUpdated: ${settings.currency}, nightMode: ${settings.theme}")
    }

    override suspend fun loadSettings(): Flow<UserSettings> {
        Log.i(TAG, "Loading settings...")
        val userSettingsFlow = context.dataStore.data.map { preferences ->
            UserSettings(
                currency = preferences[CURRENCY] ?: DEFAULT_CURRENCY,
                theme = preferences[THEME_SELECTED] ?: DEFAULT_NIGHT_MODE
            )
        }
        return userSettingsFlow
    }
}