package com.stockapp.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockapp.data.model.Holding
import com.stockapp.data.model.IPO
import com.stockapp.data.model.NewsArticle
import com.stockapp.data.model.PortfolioSummary
import com.stockapp.data.repository.PortfolioRepository
import com.stockapp.data.repository.IPORepository
import com.stockapp.data.repository.NewsRepository
import com.stockapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val portfolioRepository: PortfolioRepository,
    private val ipoRepository: IPORepository,
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<com.stockapp.data.model.User>>(emptyList())
    val users = _users

    private val _selectedUserId = MutableStateFlow<Long?>(null)
    val selectedUserId = _selectedUserId

    private val _portfolioSummary = MutableStateFlow<PortfolioSummary?>(null)
    val portfolioSummary = _portfolioSummary

    private val _topGainers = MutableStateFlow<List<Holding>>(emptyList())
    val topGainers = _topGainers

    private val _topLosers = MutableStateFlow<List<Holding>>(emptyList())
    val topLosers = _topLosers

    private val _openIPOs = MutableStateFlow<List<IPO>>(emptyList())
    val openIPOs = _openIPOs

    private val _upcomingIPOs = MutableStateFlow<List<IPO>>(emptyList())
    val upcomingIPOs = _upcomingIPOs

    private val _allotmentIPOs = MutableStateFlow<List<IPO>>(emptyList())
    val allotmentIPOs = _allotmentIPOs

    private val _latestNews = MutableStateFlow<List<NewsArticle>>(emptyList())
    val latestNews = _latestNews

    private val _highImpactNews = MutableStateFlow<List<NewsArticle>>(emptyList())
    val highImpactNews = _highImpactNews

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error = _error

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Load users
                val usersList = userRepository.getAllUsersList()
                _users.value = usersList

                // Select primary user if none selected
                if (_selectedUserId.value == null) {
                    val primaryUser = userRepository.getPrimaryUser()
                    if (primaryUser != null) {
                        _selectedUserId.value = primaryUser.id
                    } else if (usersList.isNotEmpty()) {
                        _selectedUserId.value = usersList.first().id
                    }
                }

                // Load data for selected user
                _selectedUserId.value?.let { userId ->
                    loadUserData(userId)
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadUserData(userId: Long) {
        viewModelScope.launch {
            try {
                // Portfolio summary
                _portfolioSummary.value = portfolioRepository.getPortfolioSummary(userId)

                // Top gainers/losers
                _topGainers.value = portfolioRepository.getTopGainers(userId).first()
                _topLosers.value = portfolioRepository.getTopLosers(userId).first()

                // IPOs
                _openIPOs.value = ipoRepository.getOpenIPOs().first()
                _upcomingIPOs.value = ipoRepository.getUpcomingIPOs(java.time.LocalDate.now().toString()).first()
                _allotmentIPOs.value = ipoRepository.getAllotmentIPOs().first()

                // News
                _latestNews.value = newsRepository.getLatestNews(10).first()
                _highImpactNews.value = newsRepository.getHighImpactNews(60.0, 5).first()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun selectUser(userId: Long) {
        _selectedUserId.value = userId
        loadUserData(userId)
    }

    fun refresh() {
        loadData()
    }
}