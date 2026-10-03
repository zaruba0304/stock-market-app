package com.stockapp.ui.screen.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockapp.data.model.NewsArticle
import com.stockapp.data.model.NewsPreferences
import com.stockapp.data.model.User
import com.stockapp.data.repository.NewsRepository
import com.stockapp.data.repository.UserRepository
import com.stockapp.data.repository.NetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val newsRepository: NewsRepository,
    private val networkRepository: NetworkRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users

    private val _selectedUserId = MutableStateFlow<Long?>(null)
    val selectedUserId = _selectedUserId

    private val _allNews = MutableStateFlow<List<NewsArticle>>(emptyList())
    val allNews = _allNews

    private val _portfolioNews = MutableStateFlow<List<NewsArticle>>(emptyList())
    val portfolioNews = _portfolioNews

    private val _highImpactNews = MutableStateFlow<List<NewsArticle>>(emptyList())
    val highImpactNews = _highImpactNews

    private val _unreadNews = MutableStateFlow<List<NewsArticle>>(emptyList())
    val unreadNews = _unreadNews

    private val _bookmarkedNews = MutableStateFlow<List<NewsArticle>>(emptyList())
    val bookmarkedNews = _bookmarkedNews

    private val _dailyRecap = MutableStateFlow<com.stockapp.data.model.DailyNewsRecap?>(null)
    val dailyRecap = _dailyRecap

    private val _preferences = MutableStateFlow<NewsPreferences?>(null)
    val preferences = _preferences

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing

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
                val usersList = userRepository.getAllUsersList()
                _users.value = usersList

                if (_selectedUserId.value == null) {
                    val primaryUser = userRepository.getPrimaryUser()
                    _selectedUserId.value = primaryUser?.id ?: usersList.firstOrNull()?.id
                }

                _selectedUserId.value?.let { userId ->
                    loadUserNews(userId)
                    loadPreferences(userId)
                }

                loadGeneralNews()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadUserNews(userId: Long) {
        viewModelScope.launch {
            try {
                // Get user's holdings symbols
                // For now, load general high-impact news
                _highImpactNews.value = newsRepository.getHighImpactNews(60.0, 20).first()
                _unreadNews.value = newsRepository.getUnreadNews(20).first()
                _bookmarkedNews.value = newsRepository.getBookmarkedNews().first()

                // Load today's recap
                val today = java.time.LocalDate.now().toString()
                _dailyRecap.value = newsRepository.getDailyRecap(today)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun loadPreferences(userId: Long) {
        viewModelScope.launch {
            try {
                _preferences.value = newsRepository.getPreferences(userId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun loadGeneralNews() {
        viewModelScope.launch {
            try {
                _allNews.value = newsRepository.getLatestNews(50).first()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun selectUser(userId: Long) {
        _selectedUserId.value = userId
        loadUserNews(userId)
        loadPreferences(userId)
    }

    fun loadNewsForSymbol(symbol: String) {
        viewModelScope.launch {
            try {
                _portfolioNews.value = newsRepository.getNewsBySymbol(symbol, 20).first()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun markAsRead(newsId: Long) {
        viewModelScope.launch {
            try {
                newsRepository.markAsRead(newsId)
                // Update local lists
                _unreadNews.value = _unreadNews.value.filter { it.id != newsId }
                _allNews.value = _allNews.value.map { if (it.id == newsId) it.copy(isRead = true) else it }
                _portfolioNews.value = _portfolioNews.value.map { if (it.id == newsId) it.copy(isRead = true) else it }
                _highImpactNews.value = _highImpactNews.value.map { if (it.id == newsId) it.copy(isRead = true) else it }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun markMultipleAsRead(newsIds: List<Long>) {
        viewModelScope.launch {
            try {
                newsRepository.markMultipleAsRead(newsIds)
                val idsSet = newsIds.toSet()
                _unreadNews.value = _unreadNews.value.filter { it.id !in idsSet }
                _allNews.value = _allNews.value.map { if (it.id in idsSet) it.copy(isRead = true) else it }
                _portfolioNews.value = _portfolioNews.value.map { if (it.id in idsSet) it.copy(isRead = true) else it }
                _highImpactNews.value = _highImpactNews.value.map { if (it.id in idsSet) it.copy(isRead = true) else it }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun toggleBookmark(newsId: Long, bookmarked: Boolean) {
        viewModelScope.launch {
            try {
                newsRepository.setBookmarked(newsId, bookmarked)
                _bookmarkedNews.value = if (bookmarked) {
                    _bookmarkedNews.value + _allNews.value.firstOrNull { it.id == newsId }?.copy(isBookmarked = true)
                } else {
                    _bookmarkedNews.value.filter { it.id != newsId }
                }
                _allNews.value = _allNews.value.map { if (it.id == newsId) it.copy(isBookmarked = bookmarked) else it }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun refreshNews() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                _selectedUserId.value?.let { userId ->
                    val result = networkRepository.fetchNews(minImpact = _preferences.value?.minImpactScore ?: 30.0)
                    when (result) {
                        is NetworkRepository.Result.Success -> {
                            newsRepository.insertNewsList(result.data)
                            loadUserNews(userId)
                        }
                        is NetworkRepository.Result.Failure -> {
                            _error.value = result.exception.message
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun updatePreferences(preferences: NewsPreferences) {
        viewModelScope.launch {
            try {
                newsRepository.insertOrUpdatePreferences(preferences)
                _preferences.value = preferences
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadDailyRecap(date: String) {
        viewModelScope.launch {
            try {
                _dailyRecap.value = newsRepository.getDailyRecap(date)
                if (_dailyRecap.value == null) {
                    val result = networkRepository.fetchDailyRecap(date)
                    when (result) {
                        is NetworkRepository.Result.Success -> {
                            val recap = com.stockapp.data.model.DailyNewsRecap(
                                date = date,
                                summary = result.data.summary,
                                topNews = result.data.topNews.map { it.id.toLong() },
                                marketSummary = result.data.marketSummary,
                                portfolioImpact = result.data.portfolioImpact
                            )
                            newsRepository.insertDailyRecap(recap)
                            _dailyRecap.value = recap
                        }
                        is NetworkRepository.Result.Failure -> {
                            _error.value = result.exception.message
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}