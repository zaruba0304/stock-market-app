package com.stockapp.ui.screen.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockapp.data.model.AIAnalysis
import com.stockapp.data.model.AIAnalysisType
import com.stockapp.data.model.Holding
import com.stockapp.data.model.User
import com.stockapp.data.repository.ScreenerRepository
import com.stockapp.data.repository.PortfolioRepository
import com.stockapp.data.repository.UserRepository
import com.stockapp.data.repository.NetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val portfolioRepository: PortfolioRepository,
    private val screenerRepository: ScreenerRepository,
    private val networkRepository: NetworkRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users

    private val _selectedUserId = MutableStateFlow<Long?>(null)
    val selectedUserId = _selectedUserId

    private val _portfolioAnalysis = MutableStateFlow<String?>(null)
    val portfolioAnalysis = _portfolioAnalysis

    private val _stockAnalysis = MutableStateFlow<String?>(null)
    val stockAnalysis = _stockAnalysis

    private val _newsAnalysis = MutableStateFlow<String?>(null)
    val newsAnalysis = _newsAnalysis

    private val _chatResponse = MutableStateFlow<String?>(null)
    val chatResponse = _chatResponse

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory = _chatHistory

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing = _isAnalyzing

    private val _isChatting = MutableStateFlow(false)
    val isChatting = _isChatting

    private val _error = MutableStateFlow<String?>(null)
    val error = _error

    private val _analysisType = MutableStateFlow<AIAnalysisType>(AIAnalysisType.GENERAL_QUERY)
    val analysisType = _analysisType

    data class ChatMessage(
        val isUser: Boolean,
        val message: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            try {
                val usersList = userRepository.getAllUsersList()
                _users.value = usersList

                if (_selectedUserId.value == null) {
                    val primaryUser = userRepository.getPrimaryUser()
                    _selectedUserId.value = primaryUser?.id ?: usersList.firstOrNull()?.id
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun selectUser(userId: Long) {
        _selectedUserId.value = userId
    }

    fun analyzePortfolio() {
        _selectedUserId.value?.let { userId ->
            viewModelScope.launch {
                _isAnalyzing.value = true
                _analysisType.value = AIAnalysisType.PORTFOLIO_REVIEW
                _error.value = null

                try {
                    val holdings = portfolioRepository.getHoldings(userId).first()
                    if (holdings.isEmpty()) {
                        _portfolioAnalysis.value = "No holdings found for this user."
                        return@launch
                    }

                    val result = networkRepository.analyzePortfolio(holdings)
                    when (result) {
                        is NetworkRepository.Result.Success -> {
                            _portfolioAnalysis.value = result.data.analysis
                            saveAnalysis(AIAnalysisType.PORTFOLIO_REVIEW, userId, null, result.data.analysis, result.data.confidence)
                        }
                        is NetworkRepository.Result.Failure -> {
                            _error.value = result.exception.message
                        }
                    }
                } catch (e: Exception) {
                    _error.value = e.message
                } finally {
                    _isAnalyzing.value = false
                }
            }
        }
    }

    fun analyzeStock(symbol: String, name: String, currentPrice: Double) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _analysisType.value = AIAnalysisType.STOCK_ANALYSIS
            _error.value = null

            try {
                val result = networkRepository.analyzeStock(symbol, name, currentPrice)
                when (result) {
                    is NetworkRepository.Result.Success -> {
                        _stockAnalysis.value = result.data.analysis
                        saveAnalysis(AIAnalysisType.STOCK_ANALYSIS, _selectedUserId.value ?: 0, symbol, result.data.analysis, result.data.confidence)
                    }
                    is NetworkRepository.Result.Failure -> {
                        _error.value = result.exception.message
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun analyzeNews(articles: List<com.stockapp.data.remote.ApiInterfaces.NewsArticleData>, symbols: List<String>) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _analysisType.value = AIAnalysisType.NEWS_SUMMARY
            _error.value = null

            try {
                val result = networkRepository.analyzeNews(articles, symbols)
                when (result) {
                    is NetworkRepository.Result.Success -> {
                        _newsAnalysis.value = result.data.analysis
                        saveAnalysis(AIAnalysisType.NEWS_SUMMARY, _selectedUserId.value ?: 0, null, result.data.analysis, result.data.confidence)
                    }
                    is NetworkRepository.Result.Failure -> {
                        _error.value = result.exception.message
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun sendChatMessage(message: String) {
        viewModelScope.launch {
            _isChatting.value = true
            _chatHistory.value = _chatHistory.value + ChatMessage(true, message)
            _error.value = null

            try {
                val context = buildContext()
                val result = networkRepository.chat(message, context)
                when (result) {
                    is NetworkRepository.Result.Success -> {
                        _chatResponse.value = result.data.response
                        _chatHistory.value = _chatHistory.value + ChatMessage(false, result.data.response)
                    }
                    is NetworkRepository.Result.Failure -> {
                        _error.value = result.exception.message
                        _chatHistory.value = _chatHistory.value + ChatMessage(false, "Error: ${result.exception.message}")
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
                _chatHistory.value = _chatHistory.value + ChatMessage(false, "Error: ${e.message}")
            } finally {
                _isChatting.value = false
            }
        }
    }

    private fun buildContext(): String {
        val context = StringBuilder()
        _selectedUserId.value?.let { userId ->
            val holdings = portfolioRepository.getHoldings(userId).first()
            if (holdings.isNotEmpty()) {
                context.append("Portfolio: ").append(holdings.joinToString(", ") { "${it.symbol} (${it.quantity} shares)" }).append("\n")
            }
        }
        return context.toString()
    }

    private fun saveAnalysis(
        type: AIAnalysisType,
        userId: Long,
        symbol: String?,
        response: String,
        confidence: Double
    ) {
        viewModelScope.launch {
            try {
                val analysis = com.stockapp.data.model.AIAnalysis(
                    type = type,
                    symbol = symbol,
                    userId = if (userId > 0) userId else null,
                    query = "",
                    response = response,
                    confidence = confidence
                )
                screenerRepository.insertAnalysis(analysis)
            } catch (e: Exception) {
                // Ignore save errors
            }
        }
    }

    fun clearChat() {
        _chatHistory.value = emptyList()
        _chatResponse.value = null
    }

    fun clearAnalysis() {
        _portfolioAnalysis.value = null
        _stockAnalysis.value = null
        _newsAnalysis.value = null
    }
}