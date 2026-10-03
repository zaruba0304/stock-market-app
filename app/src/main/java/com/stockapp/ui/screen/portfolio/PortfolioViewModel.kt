package com.stockapp.ui.screen.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockapp.data.model.Holding
import com.stockapp.data.model.PortfolioSummary
import com.stockapp.data.model.Transaction
import com.stockapp.data.model.User
import com.stockapp.data.repository.PortfolioRepository
import com.stockapp.data.repository.UserRepository
import com.stockapp.data.repository.NetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val portfolioRepository: PortfolioRepository,
    private val networkRepository: NetworkRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users

    private val _selectedUserId = MutableStateFlow<Long?>(null)
    val selectedUserId = _selectedUserId

    private val _holdings = MutableStateFlow<List<Holding>>(emptyList())
    val holdings = _holdings

    private val _portfolioSummary = MutableStateFlow<PortfolioSummary?>(null)
    val portfolioSummary = _portfolioSummary

    private val _topGainers = MutableStateFlow<List<Holding>>(emptyList())
    val topGainers = _topGainers

    private val _topLosers = MutableStateFlow<List<Holding>>(emptyList())
    val topLosers = _topLosers

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions = _transactions

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing

    private val _error = MutableStateFlow<String?>(null)
    val error = _error

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus = _syncStatus

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
                    loadUserPortfolio(userId)
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadUserPortfolio(userId: Long) {
        viewModelScope.launch {
            try {
                _holdings.value = portfolioRepository.getHoldings(userId).first()
                _portfolioSummary.value = portfolioRepository.getPortfolioSummary(userId)
                _topGainers.value = portfolioRepository.getTopGainers(userId).first()
                _topLosers.value = portfolioRepository.getTopLosers(userId).first()
                _transactions.value = portfolioRepository.getTransactions(userId).first()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun selectUser(userId: Long) {
        _selectedUserId.value = userId
        loadUserPortfolio(userId)
    }

    fun syncPortfolio() {
        _selectedUserId.value?.let { userId ->
            viewModelScope.launch {
                _isSyncing.value = true
                _syncStatus.value = "Syncing portfolio..."
                _error.value = null

                try {
                    val user = userRepository.getUserById(userId)
                    user?.let {
                        val result = networkRepository.syncHoldings(it)
                        when (result) {
                            is NetworkRepository.Result.Success -> {
                                val holdings = result.data
                                portfolioRepository.insertHoldings(holdings)
                                _syncStatus.value = "Synced ${holdings.size} holdings successfully"
                                loadUserPortfolio(userId)
                            }
                            is NetworkRepository.Result.Failure -> {
                                _syncStatus.value = "Sync failed: ${result.exception.message}"
                                _error.value = result.exception.message
                            }
                        }
                    }
                } catch (e: Exception) {
                    _syncStatus.value = "Error: ${e.message}"
                    _error.value = e.message
                } finally {
                    _isSyncing.value = false
                }
            }
        }
    }

    fun refresh() {
        loadData()
    }

    // Combined portfolio across all users
    val combinedPortfolioSummary = combine(users, portfolioRepository.getAllPortfolioSummaries()) { usersList, summaries ->
        val totalInvested = summaries.sumOf { it.totalInvested }
        val currentValue = summaries.sumOf { it.currentValue }
        val totalGainLoss = summaries.sumOf { it.totalGainLoss }
        val totalGainLossPercent = if (totalInvested > 0) (totalGainLoss / totalInvested) * 100 else 0.0
        val dayChange = summaries.sumOf { it.dayChange }
        val dayChangePercent = if (currentValue > 0) (dayChange / (currentValue - dayChange)) * 100 else 0.0

        PortfolioSummary(
            userId = 0, // Combined
            totalInvested = totalInvested,
            currentValue = currentValue,
            totalGainLoss = totalGainLoss,
            totalGainLossPercent = totalGainLossPercent,
            dayChange = dayChange,
            dayChangePercent = dayChangePercent
        )
    }
}