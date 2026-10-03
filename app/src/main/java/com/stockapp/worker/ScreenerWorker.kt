package com.stockapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.stockapp.data.repository.ScreenerRepository
import com.stockapp.data.repository.NetworkRepository
import com.stockapp.data.model.Screener
import com.stockapp.data.model.ScreenerResult
import com.stockapp.data.model.WeeklyStockPick
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class ScreenerWorker @Inject constructor(
    context: Context,
    params: WorkerParameters,
    private val screenerRepository: ScreenerRepository,
    private val networkRepository: NetworkRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val screeners = screenerRepository.getActiveScreeners().first()

            for (screener in screeners) {
                try {
                    val results = runScreener(screener)
                    screenerRepository.saveScreenerResults(screener.id!!, results)

                    // Generate weekly picks from top results
                    if (screener.type.name in setOf("FUNDAMENTAL", "GROWTH", "VALUE")) {
                        val picks = generateWeeklyPicks(screener, results)
                        screenerRepository.saveWeeklyPicks(picks)
                    }
                } catch (e: Exception) {
                    android.util.Log.w("ScreenerWorker", "Failed to run screener ${screener.name}", e)
                }
            }

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("ScreenerWorker", "Worker failed", e)
            Result.retry()
        }
    }

    private suspend fun runScreener(screener: Screener): List<ScreenerResult> {
        // Fetch universe of stocks (NSE 500 or similar)
        val universe = networkRepository.fetchStockUniverse()

        return universe.map { stock ->
            val score = calculateScore(stock, screener.criteria)
            val passes = score >= (screener.criteria["minScore"]?.toDouble() ?: 60.0)

            ScreenerResult(
                screenerId = screener.id!!,
                symbol = stock.symbol,
                name = stock.name,
                currentPrice = stock.currentPrice,
                changePercent = stock.changePercent,
                volume = stock.volume,
                marketCap = stock.marketCap,
                peRatio = stock.peRatio,
                pbRatio = stock.pbRatio,
                roe = stock.roe,
                debtToEquity = stock.debtToEquity,
                revenueGrowth = stock.revenueGrowth,
                profitGrowth = stock.profitGrowth,
                score = score,
                passesCriteria = passes,
                matchedCriteria = getMatchedCriteria(stock, screener.criteria),
                generatedAt = LocalDateTime.now()
            )
        }.filter { it.passesCriteria }
            .sortedByDescending { it.score }
            .take(50)
    }

    private fun calculateScore(stock: com.stockapp.data.model.StockData, criteria: Map<String, Any>): Double {
        var score = 0.0
        var factors = 0

        // P/E Ratio
        criteria["maxPE"]?.let { maxPE ->
            stock.peRatio?.let { pe ->
                if (pe <= maxPE.toDouble()) score += 20
                factors++
            }
        }

        // P/B Ratio
        criteria["maxPB"]?.let { maxPB ->
            stock.pbRatio?.let { pb ->
                if (pb <= maxPB.toDouble()) score += 15
                factors++
            }
        }

        // ROE
        criteria["minROE"]?.let { minROE ->
            stock.roe?.let { roe ->
                if (roe >= minROE.toDouble()) score += 20
                factors++
            }
        }

        // Debt to Equity
        criteria["maxDebtToEquity"]?.let { maxDTE ->
            stock.debtToEquity?.let { dte ->
                if (dte <= maxDTE.toDouble()) score += 15
                factors++
            }
        }

        // Revenue Growth
        criteria["minRevenueGrowth"]?.let { minGrowth ->
            stock.revenueGrowth?.let { growth ->
                if (growth >= minGrowth.toDouble()) score += 15
                factors++
            }
        }

        // Profit Growth
        criteria["minProfitGrowth"]?.let { minGrowth ->
            stock.profitGrowth?.let { growth ->
                if (growth >= minGrowth.toDouble()) score += 15
                factors++
            }
        }

        // Normalize score
        return if (factors > 0) (score / factors) * (factors / 6.0) * 100 else 0.0
    }

    private fun getMatchedCriteria(stock: com.stockapp.data.model.StockData, criteria: Map<String, Any>): List<String> {
        val matched = mutableListOf<String>()

        criteria["maxPE"]?.let { maxPE ->
            stock.peRatio?.let { if (it <= maxPE.toDouble()) matched.add("P/E ≤ $maxPE") }
        }
        criteria["maxPB"]?.let { maxPB ->
            stock.pbRatio?.let { if (it <= maxPB.toDouble()) matched.add("P/B ≤ $maxPB") }
        }
        criteria["minROE"]?.let { minROE ->
            stock.roe?.let { if (it >= minROE.toDouble()) matched.add("ROE ≥ $minROE%") }
        }
        criteria["maxDebtToEquity"]?.let { maxDTE ->
            stock.debtToEquity?.let { if (it <= maxDTE.toDouble()) matched.add("D/E ≤ $maxDTE") }
        }
        criteria["minRevenueGrowth"]?.let { minGrowth ->
            stock.revenueGrowth?.let { if (it >= minGrowth.toDouble()) matched.add("Rev Growth ≥ $minGrowth%") }
        }
        criteria["minProfitGrowth"]?.let { minGrowth ->
            stock.profitGrowth?.let { if (it >= minGrowth.toDouble()) matched.add("Profit Growth ≥ $minGrowth%") }
        }

        return matched
    }

    private fun generateWeeklyPicks(screener: Screener, results: List<ScreenerResult>): List<WeeklyStockPick> {
        return results.take(5).mapIndexed { index, result ->
            val currentPrice = result.currentPrice
            val targetPrice = currentPrice * 1.15 // 15% target
            val stopLoss = currentPrice * 0.92 // 8% stop loss

            WeeklyStockPick(
                screenerId = screener.id!!,
                symbol = result.symbol,
                name = result.name,
                currentPrice = currentPrice,
                targetPrice = targetPrice,
                stopLoss = stopLoss,
                reason = "Strong fundamentals: ${result.matchedCriteria.joinToString(", ")}",
                riskLevel = when {
                    result.score >= 80 -> com.stockapp.data.model.RiskLevel.LOW
                    result.score >= 65 -> com.stockapp.data.model.RiskLevel.MEDIUM
                    else -> com.stockapp.data.model.RiskLevel.HIGH
                },
                confidence = (result.score / 100) * 0.9,
                timeHorizon = com.stockapp.data.model.TimeHorizon.MEDIUM_TERM,
                weekStartDate = LocalDate.now(),
                aiGenerated = false,
                generatedAt = LocalDateTime.now()
            )
        }
    }

    companion object {
        const val WORK_NAME = "ScreenerWorker"
    }
}