package com.stockapp.data.repository

import com.stockapp.data.local.AppDatabase
import com.stockapp.data.local.dao.BrokerageCredentialsDao
import com.stockapp.data.local.dao.SettingsDao
import com.stockapp.data.model.AppSettings
import com.stockapp.data.model.BrokerageCredentials
import com.stockapp.data.model.Language
import com.stockapp.data.model.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val database: AppDatabase
) {
    private val settingsDao: SettingsDao = database.settingsDao()
    private val credentialsDao: BrokerageCredentialsDao = database.brokerageCredentialsDao()

    // App Settings
    fun getAllSettings(): Flow<List<AppSettings>> = settingsDao.getAll()

    suspend fun getSetting(key: String): String? = settingsDao.getValue(key)

    suspend fun getSettingOrDefault(key: String, default: String): String =
        settingsDao.getValue(key) ?: default

    suspend fun setSetting(key: String, value: String) {
        settingsDao.insert(AppSettings(key, value))
    }

    suspend fun setSettings(settings: List<AppSettings>) = settingsDao.insertAll(settings)

    suspend fun deleteSetting(key: String): Int = settingsDao.delete(key)

    // Typed settings
    suspend fun getLanguage(): Language {
        val code = getSettingOrDefault(AppSettings.KEY_LANGUAGE, Language.ENGLISH.code)
        return Language.fromCode(code)
    }

    suspend fun setLanguage(language: Language) {
        setSetting(AppSettings.KEY_LANGUAGE, language.code)
    }

    suspend fun getTheme(): Theme {
        val value = getSettingOrDefault(AppSettings.KEY_THEME, Theme.SYSTEM.value)
        return Theme.fromString(value)
    }

    suspend fun setTheme(theme: Theme) {
        setSetting(AppSettings.KEY_THEME, theme.value)
    }

    suspend fun isBiometricEnabled(): Boolean =
        getSettingOrDefault(AppSettings.KEY_BIOMETRIC_ENABLED, "false").toBoolean()

    suspend fun setBiometricEnabled(enabled: Boolean) {
        setSetting(AppSettings.KEY_BIOMETRIC_ENABLED, enabled.toString())
    }

    suspend fun areNotificationsEnabled(): Boolean =
        getSettingOrDefault(AppSettings.KEY_NOTIFICATIONS_ENABLED, "true").toBoolean()

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        setSetting(AppSettings.KEY_NOTIFICATIONS_ENABLED, enabled.toString())
    }

    suspend fun areNewsNotificationsEnabled(): Boolean =
        getSettingOrDefault(AppSettings.KEY_NEWS_NOTIFICATIONS, "true").toBoolean()

    suspend fun setNewsNotificationsEnabled(enabled: Boolean) {
        setSetting(AppSettings.KEY_NEWS_NOTIFICATIONS, enabled.toString())
    }

    suspend fun areIPONotificationsEnabled(): Boolean =
        getSettingOrDefault(AppSettings.KEY_IPO_NOTIFICATIONS, "true").toBoolean()

    suspend fun setIPONotificationsEnabled(enabled: Boolean) {
        setSetting(AppSettings.KEY_IPO_NOTIFICATIONS, enabled.toString())
    }

    suspend fun arePriceAlertsEnabled(): Boolean =
        getSettingOrDefault(AppSettings.KEY_PRICE_ALERTS, "true").toBoolean()

    suspend fun setPriceAlertsEnabled(enabled: Boolean) {
        setSetting(AppSettings.KEY_PRICE_ALERTS, enabled.toString())
    }

    suspend fun isAutoSyncEnabled(): Boolean =
        getSettingOrDefault(AppSettings.KEY_AUTO_SYNC, "true").toBoolean()

    suspend fun setAutoSyncEnabled(enabled: Boolean) {
        setSetting(AppSettings.KEY_AUTO_SYNC, enabled.toString())
    }

    suspend fun getSyncInterval(): Int =
        getSettingOrDefault(AppSettings.KEY_SYNC_INTERVAL, "15").toIntOrNull() ?: 15

    suspend fun setSyncInterval(minutes: Int) {
        setSetting(AppSettings.KEY_SYNC_INTERVAL, minutes.toString())
    }

    suspend fun isDataSaverEnabled(): Boolean =
        getSettingOrDefault(AppSettings.KEY_DATA_SAVER, "false").toBoolean()

    suspend fun setDataSaverEnabled(enabled: Boolean) {
        setSetting(AppSettings.KEY_DATA_SAVER, enabled.toString())
    }

    suspend fun isAIEnabled(): Boolean =
        getSettingOrDefault(AppSettings.KEY_AI_ENABLED, "true").toBoolean()

    suspend fun setAIEnabled(enabled: Boolean) {
        setSetting(AppSettings.KEY_AI_ENABLED, enabled.toString())
    }

    suspend fun getAIModel(): String =
        getSettingOrDefault(AppSettings.KEY_AI_MODEL, "gemini-pro")

    suspend fun setAIModel(model: String) {
        setSetting(AppSettings.KEY_AI_MODEL, model)
    }

    suspend fun isWeekendSummaryEnabled(): Boolean =
        getSettingOrDefault(AppSettings.KEY_WEEKEND_SUMMARY, "true").toBoolean()

    suspend fun setWeekendSummaryEnabled(enabled: Boolean) {
        setSetting(AppSettings.KEY_WEEKEND_SUMMARY, enabled.toString())
    }

    suspend fun isDailyRecapEnabled(): Boolean =
        getSettingOrDefault(AppSettings.KEY_DAILY_RECAP, "true").toBoolean()

    suspend fun setDailyRecapEnabled(enabled: Boolean) {
        setSetting(AppSettings.KEY_DAILY_RECAP, enabled.toString())
    }

    // Brokerage Credentials
    fun getActiveCredentials(userId: Long): Flow<List<BrokerageCredentials>> =
        credentialsDao.getActiveByUserId(userId)

    suspend fun getCredentials(userId: Long, brokerageId: String): BrokerageCredentials? =
        credentialsDao.getByUserAndBrokerage(userId, brokerageId)

    suspend fun getAllCredentials(userId: Long): List<BrokerageCredentials> =
        credentialsDao.getAllByUserId(userId)

    suspend fun saveCredentials(credentials: BrokerageCredentials): Long =
        credentialsDao.insert(credentials)

    suspend fun updateCredentials(credentials: BrokerageCredentials): Int =
        credentialsDao.update(credentials)

    suspend fun deactivateCredentials(userId: Long, brokerageId: String): Int =
        credentialsDao.deactivate(userId, brokerageId)
}