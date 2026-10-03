package com.stockapp.ui.screen.ipo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockapp.data.model.IPO
import com.stockapp.data.model.IPOApplication
import com.stockapp.data.model.IPOStatus
import com.stockapp.data.model.User
import com.stockapp.data.repository.IPORepository
import com.stockapp.data.repository.UserRepository
import com.stockapp.data.repository.NetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IPOViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val ipoRepository: IPORepository,
    private val networkRepository: NetworkRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users

    private val _selectedUserId = MutableStateFlow<Long?>(null)
    val selectedUserId = _selectedUserId

    private val _upcomingIPOs = MutableStateFlow<List<IPO>>(emptyList())
    val upcomingIPOs = _upcomingIPOs

    private val _openIPOs = MutableStateFlow<List<IPO>>(emptyList())
    val openIPOs = _openIPOs

    private val _allotmentIPOs = MutableStateFlow<List<IPO>>(emptyList())
    val allotmentIPOs = _allotmentIPOs

    private val _listedIPOs = MutableStateFlow<List<IPO>>(emptyList())
    val listedIPOs = _listedIPOs

    private val _userApplications = MutableStateFlow<List<IPOApplication>>(emptyList())
    val userApplications = _userApplications

    private val _selectedIPO = MutableStateFlow<IPO?>(null)
    val selectedIPO = _selectedIPO

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error = _error

    private val _applyStatus = MutableStateFlow<String?>(null)
    val applyStatus = _applyStatus

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val usersList = userRepository.getAllUsersList()
                _users.value = usersList

                if (_selectedUserId.value == null) {
                    val primaryUser = userRepository.getPrimaryUser()
                    _selectedUserId.value = primaryUser?.id ?: usersList.firstOrNull()?.id
                }

                _selectedUserId.value?.let { userId ->
                    loadUserApplications(userId)
                }

                loadIPOs()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadIPOs() {
        viewModelScope.launch {
            try {
                _upcomingIPOs.value = ipoRepository.getIPOsByStatus(IPOStatus.UPCOMING).first()
                _openIPOs.value = ipoRepository.getOpenIPOs().first()
                _allotmentIPOs.value = ipoRepository.getIPOsByStatus(IPOStatus.ALLOTMENT).first()
                _listedIPOs.value = ipoRepository.getIPOsByStatus(IPOStatus.LISTED).first()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun loadUserApplications(userId: Long) {
        viewModelScope.launch {
            try {
                _userApplications.value = ipoRepository.getApplicationsByUser(userId).first()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun selectUser(userId: Long) {
        _selectedUserId.value = userId
        loadUserApplications(userId)
    }

    fun selectIPO(ipo: IPO) {
        _selectedIPO.value = ipo
    }

    fun applyToIPO(ipo: IPO, lots: Int, price: Double, upiId: String?) {
        _selectedUserId.value?.let { userId ->
            viewModelScope.launch {
                _applyStatus.value = "Applying..."
                try {
                    val user = userRepository.getUserById(userId)
                    user?.let {
                        val request = com.stockapp.data.remote.ApiInterfaces.IPOApplicationRequest(
                            ipoId = ipo.id,
                            pan = it.panNumber,
                            dematAccount = it.dematAccountNumber,
                            lots = lots,
                            price = price,
                            upiId = upiId
                        )
                        val result = networkRepository.applyIPO(request)
                        when (result) {
                            is com.stockapp.data.repository.NetworkRepository.Result.Success -> {
                                _applyStatus.value = "Applied successfully! Application ID: ${result.data.applicationId}"
                                loadUserApplications(userId)
                            }
                            is com.stockapp.data.repository.NetworkRepository.Result.Failure -> {
                                _applyStatus.value = "Failed: ${result.exception.message}"
                            }
                        }
                    }
                } catch (e: Exception) {
                    _applyStatus.value = "Error: ${e.message}"
                }
            }
        }
    }

    fun checkAllotment() {
        _selectedUserId.value?.let { userId ->
            viewModelScope.launch {
                _applyStatus.value = "Checking allotment..."
                try {
                    val user = userRepository.getUserById(userId)
                    user?.let {
                        val result = networkRepository.checkAllotment(it.panNumber)
                        when (result) {
                            is com.stockapp.data.repository.NetworkRepository.Result.Success -> {
                                _applyStatus.value = "Allotment checked! ${result.data.applications.size} applications found"
                                loadUserApplications(userId)
                            }
                            is com.stockapp.data.repository.NetworkRepository.Result.Failure -> {
                                _applyStatus.value = "Failed: ${result.exception.message}"
                            }
                        }
                    }
                } catch (e: Exception) {
                    _applyStatus.value = "Error: ${e.message}"
                }
            }
        }
    }

    fun refresh() {
        loadData()
    }

    sealed class Result<out T> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Failure(val exception: Exception) : Result<Nothing>()
    }
}