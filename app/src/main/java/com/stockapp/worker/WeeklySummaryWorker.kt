package com.stockapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.stockapp.data.repository.PortfolioRepository
import com.stockapp.data.repository.ScreenerRepository
import com.stockapp.data.model.WeeklyPortfolioSummary
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class WeeklySummaryWorker @Inject constructor(
    context: Context,
    params: WorkerParameters,
    private val portfolioRepository: PortfolioRepository,
    private val screenerRepository: ScreenerRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val today = LocalDate.now()
            val weekStart = today.minusDays(6)

            // Get portfolio summary
            val portfolioSummary = portfolioRepository.getPortfolioSummary().first()

            // Get weekly performance data
            val weeklyPerformance = portfolioRepository.getWeeklyPerformance(weekStart, today).firstOrNull()

            // Get top performers and losers
            val topPerformers = portfolioRepository.getTopPerformers(weekStart, today, 5).firstOrNull() ?: emptyList()
            val topLosers = portfolioRepository.getTopLosers(weekStart, today, 5).firstOrNull() ?: emptyList()

            // Get sector allocation
            val sectorAllocation = portfolioRepository.getSectorAllocation().firstOrNull() ?: emptyMap()

            // Get weekly screener picks
            val weeklyPicks = screenerRepository.getWeeklyPicks().firstOrNull() ?: emptyList()

            // Generate insights
            val insights = generateInsights(
                portfolioSummary = portfolioSummary,
                weeklyPerformance = weeklyPerformance,
                topPerformers = topPerformers,
                topLosers = topLosers,
                sectorAllocation = sectorAllocation,
                weeklyPicks = weeklyPicks
            )

            // Create weekly summary
            val summary = WeeklyPortfolioSummary(
                weekStartDate = weekStart,
                weekEndDate = today,
                totalValueStart = weeklyPerformance?.startValue ?: portfolioSummary?.totalValue ?: 0.0,
                totalValueEnd = portfolioSummary?.totalValue ?: 0.0,
                weeklyGainLoss = weeklyPerformance?.gainLoss ?: 0.0,
                weeklyGainLossPercent = weeklyPerformance?.gainLossPercent ?: 0.0,
                topPerformers = topPerformers.map { it.symbol },
                topLosers = topLosers.map { it.symbol },
                sectorAllocation = sectorAllocation,
                keyInsights = insights,
                recommendedActions = generateActions(topPerformers, topLosers, sectorAllocation),
                weeklyPicks = weeklyPicks.map { it.symbol },
                generatedAt = LocalDateTime.now()
            )

            portfolioRepository.saveWeeklySummary(summary)

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("WeeklySummaryWorker", "Worker failed", e)
            Result.retry()
        }
    }

    private fun generateInsights(
        portfolioSummary: com.stockapp.data.model.PortfolioSummary?,
        weeklyPerformance: com.stockapp.data.model.WeeklyPerformance?,
        topPerformers: List<com.stockapp.data.model.Holding>,
        topLosers: List<com.stockapp.data.model.Holding>,
        sectorAllocation: Map<String, Double>,
        weeklyPicks: List<com.stockapp.data.model.WeeklyStockPick>
    ): List<String> {
        val insights = mutableListOf<String>()

        portfolioSummary?.let { summary ->
            val weeklyChange = weeklyPerformance?.gainLossPercent ?: 0.0
            if (weeklyChange > 2) {
                insights.add("Portfolio gained ${String.format("%.1f", weeklyChange)}% this week - strong performance!")
            } else if (weeklyChange < -2) {
                insights.add("Portfolio declined ${String.format("%.1f", weeklyChange)}% this week - consider reviewing positions")
            } else {
                insights.add("Portfolio relatively flat this week (${String.format("%.1f", weeklyChange)}%)")
            }
        }

        if (topPerformers.isNotEmpty()) {
            val best = topPerformers.first()
            insights.add("${best.symbol} was the top performer with ${String.format("%.1f", best.dayChangePercent)}% gain")
        }

        if (topLosers.isNotEmpty()) {
            val worst = topLosers.first()
            insights.add("${worst.symbol} was the biggest loser with ${String.format("%.1f", worst.dayChangePercent)}% decline")
        }

        // Sector concentration check
        sectorAllocation.entries.maxByOrNull { it.value }?.let { maxSector ->
            if (maxSector.value > 40) {
                insights.add("High concentration in ${maxSector.key} sector (${String.format("%.0f", maxSector.value)}%) - consider diversification")
            }
        }

        if (weeklyPicks.isNotEmpty()) {
            insights.add("${weeklyPicks.size} new AI-generated stock picks for next week")
        }

        return insights
    }

    private fun generateActions(
        topPerformers: List<com.stockapp.data.model.Holding>,
        topLosers: List<com.stockapp.data.model.Holding>,
        sectorAllocation: Map<String, Double>
    ): List<String> {
        val actions = mutableListOf<String>()

        if (topLosers.isNotEmpty()) {
            actions.add("Review ${topLosers.first().symbol} - consider stop-loss or averaging down")
        }

        sectorAllocation.entries.maxByOrNull { it.value }?.let { maxSector ->
            if (maxSector.value > 40) {
                actions.add("Reduce exposure to ${maxSector.key} sector for better diversification")
            }
        }

        if (topPerformers.size >= 2) {
            actions.add("Consider booking partial profits in ${topPerformers.take(2).joinToString(", ") { it.symbol }}")
        }

        actions.add("Run screeners this weekend for fresh opportunities")
        actions.add("Review upcoming IPOs for next week")

        return actions
    }

    companion object {
        const val WORK_NAME = "WeeklySummaryWorker"
    }
}