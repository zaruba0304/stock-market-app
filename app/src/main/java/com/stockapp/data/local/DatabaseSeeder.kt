package com.stockapp.data.local

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.stockapp.data.model.Settings
import com.stockapp.data.model.ThemeMode
import com.stockapp.data.model.Language
import com.stockapp.data.repository.SettingsRepository
import com.stockapp.data.repository.ScreenerRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DatabaseSeeder @Inject constructor(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val screenerRepository: ScreenerRepository
) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun seedDatabase() {
        // Seed default settings
        seedDefaultSettings()

        // Seed default screeners
        seedDefaultScreeners()
    }

    private fun seedDefaultSettings() {
        val existingSettings = settingsRepository.getSettings().firstOrNull()
        if (existingSettings == null) {
            val defaultSettings = Settings(
                language = Language.ENGLISH,
                themeMode = ThemeMode.SYSTEM,
                notificationsEnabled = true,
                biometricEnabled = false,
                autoSyncEnabled = true,
                syncIntervalMinutes = 30,
                createdAt = java.time.LocalDateTime.now(),
                updatedAt = java.time.LocalDateTime.now()
            )
            settingsRepository.saveSettings(defaultSettings)
        }
    }

    private fun seedDefaultScreeners() {
        val existingScreeners = screenerRepository.getAllScreeners().firstOrNull()
        if (existingScreeners == null || existingScreeners.isEmpty()) {
            val defaultScreeners = DefaultScreeners.getDefaultScreeners()
            defaultScreeners.forEach { screener ->
                screenerRepository.insertScreener(screener)
            }
        }
    }
}