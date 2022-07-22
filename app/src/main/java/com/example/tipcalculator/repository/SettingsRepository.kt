package com.example.tipcalculator.repository

import com.example.tipcalculator.models.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun saveSettings(settings: UserSettings)
    suspend fun loadSettings(): Flow<UserSettings>

}