package com.stockapp.ui.screen.screeners

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockapp.data.model.Screener
import com.stockapp.data.model.ScreenerResult
import com.stockapp.data.model.WeeklyStockPick
import com.stockapp.data.repository.ScreenerRepository
import com.stockapp.data.repository.NetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScreenersViewModel @Inject constructor(
    private val screenerRepository: ScreenerRepository,
    private val networkRepository: NetworkRepository
) : ViewModel() {

    private val _screeners = MutableStateFlow<List<Screener>>(emptyList())
    val screeners = _screeners

    private val _selectedScreener = MutableStateFlow<Screener?>(null)
    val selectedScreener = _selectedScreener

    private val _results = MutableStateFlow<List<ScreenerResult>>(emptyList())
    val results = _results

    private val _weeklyPicks = MutableStateFlow<List<WeeklyStockPick>>(emptyList())
    val weeklyPicks = _weeklyPicks

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning

    private val _error = MutableStateFlow<String?>(null)
    val error = _error

    private val _runStatus = MutableStateFlow<String?>(null)
    val runStatus = _runStatus

    init {
        loadScreeners()
        loadWeeklyPicks()
    }

    fun loadScreeners() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _screeners.value = screenerRepository.getActiveScreeners().first()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadWeeklyPicks() {
        viewModelScope.launch {
            try {
                val weekStart = getWeekStartDate()
                _weeklyPicks.value = screenerRepository.getPicksByWeek(weekStart).first()
                if (_weeklyPicks.value.isEmpty()) {
                    fetchWeeklyPicks(weekStart)
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun getWeekStartDate(): String {
        val now = java.time.LocalDate.now()
        val weekStart = now.minusDays(now.dayOfWeek.value.toLong() - 1) // Monday
        return weekStart.toString()
    }

    fun selectScreener(screener: Screener) {
        _selectedScreener.value = screener
        loadResults(screener.id)
    }

    fun loadResults(screenerId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _results.value = screenerRepository.getResultsByScreener(screenerId).first()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun runScreener(screener: Screener) {
        viewModelScope.launch {
            _isRunning.value = true
            _runStatus.value = "Running screener..."
            _error.value = null

            try {
                val result = networkRepository.runScreener(screener.id)
                when (result) {
                    is NetworkRepository.Result.Success -> {
                        _runStatus.value = "Screener completed with ${result.data.size} results"
                        screenerRepository.insertResults(result.data)
                        loadResults(screener.id)
                    }
                    is NetworkRepository.Result.Failure -> {
                        _runStatus.value = "Failed: ${result.exception.message}"
                        _error.value = result.exception.message
                    }
                }
            } catch (e: Exception) {
                _runStatus.value = "Error: ${e.message}"
                _error.value = e.message
            } finally {
                _isRunning.value = false
            }
        }
    }

    private fun fetchWeeklyPicks(weekStart: String) {
        viewModelScope.launch {
            _runStatus.value = "Fetching weekly picks..."
            try {
                val result = networkRepository.getWeeklyPicks(weekStart)
                when (result) {
                    is NetworkRepository.Result.Success -> {
                        val picks = result.data.map { data ->
                            com.stockapp.data.model.WeeklyStockPick(
                                weekStartDate = weekStart,
                                symbol = data.symbol,
                                name = data.name,
                                currentPrice = data.currentPrice,
                                targetPrice = data.targetPrice,
                                stopLoss = data.stopLoss,
                                reason = data.reason,
                                riskLevel = com.stockapp.data.model.RiskLevel.fromString(data.riskLevel),
                                timeHorizon = com.stockapp.data.model.TimeHorizon.fromString(data.timeHorizon),
                                screenerId = null,
                                aiGenerated = true,
                                confidence = data.confidence
                            )
                        }
                        screenerRepository.insertPicks(picks)
                        _weeklyPicks.value = picks
                        _runStatus.value = "Weekly picks updated"
                    }
                    is NetworkRepository.Result.Failure -> {
                        _runStatus.value = "Failed to fetch picks: ${result.exception.message}"
                    }
                }
            } catch (e: Exception) {
                _runStatus.value = "Error: ${e.message}"
            }
        }
    }

    fun refresh() {
        loadScreeners()
        loadWeeklyPicks()
    }
}