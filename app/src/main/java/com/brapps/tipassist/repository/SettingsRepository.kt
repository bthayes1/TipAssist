package com.brapps.tipassist.repository

import com.brapps.tipassist.models.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun saveSettings(settings: UserSettings)
    suspend fun loadSettings(): Flow<UserSettings>

}