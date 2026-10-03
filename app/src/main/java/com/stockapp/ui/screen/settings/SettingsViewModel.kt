package com.stockapp.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockapp.data.model.Brokerage
import com.stockapp.data.model.BrokerageCredentials
import com.stockapp.data.model.Language
import com.stockapp.data.model.Theme
import com.stockapp.data.model.User
import com.stockapp.data.repository.SettingsRepository
import com.stockapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users

    private val _language = MutableStateFlow<Language>(Language.ENGLISH)
    val language = _language

    private val _theme = MutableStateFlow<Theme>(Theme.SYSTEM)
    val theme = _theme

    private val _biometricEnabled = MutableStateFlow(false)
    val biometricEnabled = _biometricEnabled

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled = _notificationsEnabled

    private val _newsNotifications = MutableStateFlow(true)
    val newsNotifications = _newsNotifications

    private val _ipoNotifications = MutableStateFlow(true)
    val ipoNotifications = _ipoNotifications

    private val _priceAlerts = MutableStateFlow(true)
    val priceAlerts = _priceAlerts

    private val _autoSync = MutableStateFlow(true)
    val autoSync = _autoSync

    private val _syncInterval = MutableStateFlow(15)
    val syncInterval = _syncInterval

    private val _dataSaver = MutableStateFlow(false)
    val dataSaver = _dataSaver

    private val _aiEnabled = MutableStateFlow(true)
    val aiEnabled = _aiEnabled

    private val _aiModel = MutableStateFlow("gemini-pro")
    val aiModel = _aiModel

    private val _weekendSummary = MutableStateFlow(true)
    val weekendSummary = _weekendSummary

    private val _dailyRecap = MutableStateFlow(true)
    val dailyRecap = _dailyRecap

    private val _brokerageCredentials = MutableStateFlow<List<BrokerageCredentials>>(emptyList())
    val brokerageCredentials = _brokerageCredentials

    private val _selectedUserId = MutableStateFlow<Long?>(null)
    val selectedUserId = _selectedUserId

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error = _error

    private val _saveStatus = MutableStateFlow<String?>(null)
    val saveStatus = _saveStatus

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val usersList = userRepository.getAllUsersList()
                _users.value = usersList

                if (_selectedUserId.value == null) {
                    val primaryUser = userRepository.getPrimaryUser()
                    _selectedUserId.value = primaryUser?.id ?: usersList.firstOrNull()?.id
                }

                _selectedUserId.value?.let { userId ->
                    loadUserCredentials(userId)
                }

                // Load app settings
                _language.value = settingsRepository.getLanguage()
                _theme.value = settingsRepository.getTheme()
                _biometricEnabled.value = settingsRepository.isBiometricEnabled()
                _notificationsEnabled.value = settingsRepository.areNotificationsEnabled()
                _newsNotifications.value = settingsRepository.areNewsNotificationsEnabled()
                _ipoNotifications.value = settingsRepository.areIPONotificationsEnabled()
                _priceAlerts.value = settingsRepository.arePriceAlertsEnabled()
                _autoSync.value = settingsRepository.isAutoSyncEnabled()
                _syncInterval.value = settingsRepository.getSyncInterval()
                _dataSaver.value = settingsRepository.isDataSaverEnabled()
                _aiEnabled.value = settingsRepository.isAIEnabled()
                _aiModel.value = settingsRepository.getAIModel()
                _weekendSummary.value = settingsRepository.isWeekendSummaryEnabled()
                _dailyRecap.value = settingsRepository.isDailyRecapEnabled()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadUserCredentials(userId: Long) {
        viewModelScope.launch {
            try {
                _brokerageCredentials.value = settingsRepository.getAllCredentials(userId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun selectUser(userId: Long) {
        _selectedUserId.value = userId
        loadUserCredentials(userId)
    }

    fun setLanguage(language: Language) {
        _language.value = language
        viewModelScope.launch {
            settingsRepository.setLanguage(language)
            _saveStatus.value = "Language changed to ${language.name}"
        }
    }

    fun setTheme(theme: Theme) {
        _theme.value = theme
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
            _saveStatus.value = "Theme changed to ${theme.value}"
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        _biometricEnabled.value = enabled
        viewModelScope.launch {
            settingsRepository.setBiometricEnabled(enabled)
            _saveStatus.value = if (enabled) "Biometric enabled" else "Biometric disabled"
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
        }
    }

    fun setNewsNotifications(enabled: Boolean) {
        _newsNotifications.value = enabled
        viewModelScope.launch {
            settingsRepository.setNewsNotificationsEnabled(enabled)
        }
    }

    fun setIPONotifications(enabled: Boolean) {
        _ipoNotifications.value = enabled
        viewModelScope.launch {
            settingsRepository.setIPONotificationsEnabled(enabled)
        }
    }

    fun setPriceAlerts(enabled: Boolean) {
        _priceAlerts.value = enabled
        viewModelScope.launch {
            settingsRepository.setPriceAlertsEnabled(enabled)
        }
    }

    fun setAutoSync(enabled: Boolean) {
        _autoSync.value = enabled
        viewModelScope.launch {
            settingsRepository.setAutoSyncEnabled(enabled)
        }
    }

    fun setSyncInterval(minutes: Int) {
        _syncInterval.value = minutes
        viewModelScope.launch {
            settingsRepository.setSyncInterval(minutes)
        }
    }

    fun setDataSaver(enabled: Boolean) {
        _dataSaver.value = enabled
        viewModelScope.launch {
            settingsRepository.setDataSaverEnabled(enabled)
        }
    }

    fun setAIEnabled(enabled: Boolean) {
        _aiEnabled.value = enabled
        viewModelScope.launch {
            settingsRepository.setAIEnabled(enabled)
        }
    }

    fun setAIModel(model: String) {
        _aiModel.value = model
        viewModelScope.launch {
            settingsRepository.setAIModel(model)
        }
    }

    fun setWeekendSummary(enabled: Boolean) {
        _weekendSummary.value = enabled
        viewModelScope.launch {
            settingsRepository.setWeekendSummaryEnabled(enabled)
        }
    }

    fun setDailyRecap(enabled: Boolean) {
        _dailyRecap.value = enabled
        viewModelScope.launch {
            settingsRepository.setDailyRecapEnabled(enabled)
        }
    }

    fun saveBrokerageCredentials(credentials: BrokerageCredentials) {
        _selectedUserId.value?.let { userId ->
            viewModelScope.launch {
                _saveStatus.value = "Saving credentials..."
                try {
                    val updated = credentials.copy(userId = userId)
                    settingsRepository.saveCredentials(updated)
                    loadUserCredentials(userId)
                    _saveStatus.value = "Credentials saved for ${Brokerage.fromId(credentials.brokerageId).name}"
                } catch (e: Exception) {
                    _error.value = e.message
                    _saveStatus.value = "Failed to save: ${e.message}"
                }
            }
        }
    }

    fun deleteBrokerageCredentials(brokerageId: String) {
        _selectedUserId.value?.let { userId ->
            viewModelScope.launch {
                try {
                    settingsRepository.deactivateCredentials(userId, brokerageId)
                    loadUserCredentials(userId)
                    _saveStatus.value = "Credentials removed for ${Brokerage.fromId(brokerageId).name}"
                } catch (e: Exception) {
                    _error.value = e.message
                }
            }
        }
    }

    fun addUser(name: String, pan: String, dematAccount: String, brokerageId: String, isPrimary: Boolean) {
        viewModelScope.launch {
            try {
                val user = User(
                    name = name,
                    panNumber = pan,
                    dematAccountNumber = dematAccount,
                    brokerageId = brokerageId,
                    brokerageUserId = "",
                    isPrimary = isPrimary
                )
                userRepository.insertUser(user)
                val usersList = userRepository.getAllUsersList()
                _users.value = usersList
                if (isPrimary) {
                    userRepository.setPrimaryUser(user.id!!)
                }
                _saveStatus.value = "User added successfully"
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            try {
                userRepository.deleteUser(userId)
                val usersList = userRepository.getAllUsersList()
                _users.value = usersList
                if (_selectedUserId.value == userId) {
                    _selectedUserId.value = usersList.firstOrNull()?.id
                }
                _saveStatus.value = "User deleted"
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    val availableBrokers = Brokerage.all
    val availableLanguages = Language.values()
    val availableThemes = Theme.values()
    val syncIntervals = listOf(5, 15, 30, 60, 120)
}