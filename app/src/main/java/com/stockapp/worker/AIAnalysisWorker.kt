package com.stockapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.stockapp.data.repository.AIRepository
import com.stockapp.data.repository.PortfolioRepository
import com.stockapp.data.repository.NewsRepository
import com.stockapp.data.model.AIAnalysis
import com.stockapp.data.model.AIAnalysisType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class AIAnalysisWorker @Inject constructor(
    context: Context,
    params: WorkerParameters,
    private val aiRepository: AIRepository,
    private val portfolioRepository: PortfolioRepository,
    private val newsRepository: NewsRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Generate portfolio review
            generatePortfolioReview()

            // Generate market outlook
            generateMarketOutlook()

            // Generate risk assessment
            generateRiskAssessment()

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("AIAnalysisWorker", "Worker failed", e)
            Result.retry()
        }
    }

    private suspend fun generatePortfolioReview() {
        val portfolioSummary = portfolioRepository.getPortfolioSummary().firstOrNull()
        val holdings = portfolioRepository.getAllHoldings().firstOrNull() ?: emptyList()

        if (holdings.isEmpty()) return

        val prompt = buildPortfolioReviewPrompt(portfolioSummary, holdings)
        val analysis = aiRepository.analyze(prompt, AIAnalysisType.PORTFOLIO_REVIEW)

        aiRepository.saveAnalysis(analysis.copy(
            type = AIAnalysisType.PORTFOLIO_REVIEW,
            title = "Weekly Portfolio Review",
            summary = analysis.summary.take(500)
        ))
    }

    private suspend fun generateMarketOutlook() {
        val news = newsRepository.getLatestNews(20).firstOrNull() ?: emptyList()
        val prompt = buildMarketOutlookPrompt(news)
        val analysis = aiRepository.analyze(prompt, AIAnalysisType.MARKET_OUTLOOK)

        aiRepository.saveAnalysis(analysis.copy(
            type = AIAnalysisType.MARKET_OUTLOOK,
            title = "Market Outlook",
            summary = analysis.summary.take(500)
        ))
    }

    private suspend fun generateRiskAssessment() {
        val portfolioSummary = portfolioRepository.getPortfolioSummary().firstOrNull()
        val holdings = portfolioRepository.getAllHoldings().firstOrNull() ?: emptyList()

        if (holdings.isEmpty()) return

        val prompt = buildRiskAssessmentPrompt(portfolioSummary, holdings)
        val analysis = aiRepository.analyze(prompt, AIAnalysisType.RISK_ASSESSMENT)

        aiRepository.saveAnalysis(analysis.copy(
            type = AIAnalysisType.RISK_ASSESSMENT,
            title = "Risk Assessment",
            summary = analysis.summary.take(500)
        ))
    }

    private fun buildPortfolioReviewPrompt(
        summary: com.stockapp.data.model.PortfolioSummary?,
        holdings: List<com.stockapp.data.model.Holding>
    ): String {
        val builder = StringBuilder()
        builder.append("Analyze this Indian stock portfolio and provide a concise review:\n\n")

        summary?.let {
            builder.append("Total Value: ₹${String.format("%.2f", it.totalValue)}\n")
            builder.append("Total Invested: ₹${String.format("%.2f", it.totalInvested)}\n")
            builder.append("Total P&L: ₹${String.format("%.2f", it.totalGainLoss)} (${String.format("%.2f", it.totalGainLossPercent)}%)\n")
            builder.append("Day Change: ₹${String.format("%.2f", it.dayChange)} (${String.format("%.2f", it.dayChangePercent)}%)\n")
            builder.append("Holdings Count: ${it.holdingsCount}\n\n")
        }

        builder.append("Top Holdings:\n")
        holdings.sortedByDescending { it.currentValue }.take(10).forEach { holding ->
            builder.append("- ${holding.symbol}: ${holding.quantity} shares @ ₹${String.format("%.2f", holding.averagePrice)} = ₹${String.format("%.2f", holding.currentValue)} (${if (holding.dayChangePercent >= 0) "+" else ""}${String.format("%.2f", holding.dayChangePercent)}% today)\n")
        }

        builder.append("\nProvide:\n")
        builder.append("1. Overall portfolio health assessment\n")
        builder.append("2. Top 3 strengths\n")
        builder.append("3. Top 3 risks/concerns\n")
        builder.append("4. Specific actionable recommendations\n")
        builder.append("5. Sector diversification analysis\n")
        builder.append("6. Risk level (LOW/MEDIUM/HIGH)\n")
        builder.append("7. Confidence score (0-1)\n")

        return builder.toString()
    }

    private fun buildMarketOutlookPrompt(news: List<com.stockapp.data.model.NewsArticle>): String {
        val builder = StringBuilder()
        builder.append("Analyze current Indian market conditions based on recent news:\n\n")

        news.take(10).forEach { article ->
            builder.append("- ${article.title} (${article.source}, ${article.sentiment.value}, ${article.impactLevel.value})\n")
        }

        builder.append("\nProvide:\n")
        builder.append("1. Market sentiment (BULLISH/BEARISH/NEUTRAL)\n")
        builder.append("2. Key themes driving the market\n")
        builder.append("3. Sectors to watch\n")
        builder.append("4. Near-term outlook (1-4 weeks)\n")
        builder.append("5. Key risks\n")
        builder.append("6. Confidence score (0-1)\n")

        return builder.toString()
    }

    private fun buildRiskAssessmentPrompt(
        summary: com.stockapp.data.model.PortfolioSummary?,
        holdings: List<com.stockapp.data.model.Holding>
    ): String {
        val builder = StringBuilder()
        builder.append("Assess risk for this Indian stock portfolio:\n\n")

        summary?.let {
            builder.append("Portfolio Value: ₹${String.format("%.2f", it.totalValue)}\n")
            builder.append("Total P&L: ${String.format("%.2f", it.totalGainLossPercent)}%\n\n")
        }

        // Concentration analysis
        val totalValue = holdings.sumOf { it.currentValue }
        val top5Concentration = holdings.sortedByDescending { it.currentValue }.take(5).sumOf { it.currentValue } / totalValue * 100
        builder.append("Top 5 concentration: ${String.format("%.1f", top5Concentration)}%\n")

        // Sector analysis
        val sectorAllocation = holdings.groupBy { it.sector ?: "Unknown" }
            .mapValues { (_, h) -> h.sumOf { it.currentValue } / totalValue * 100 }
            .toSortedMap()
        builder.append("Sector allocation:\n")
        sectorAllocation.forEach { (sector, pct) ->
            builder.append("- $sector: ${String.format("%.1f", pct)}%\n")
        }

        builder.append("\nProvide:\n")
        builder.append("1. Overall risk level (LOW/MEDIUM/HIGH)\n")
        builder.append("2. Concentration risk analysis\n")
        builder.append("3. Sector risk analysis\n")
        builder.append("4. Liquidity risk\n")
        builder.append("5. Specific risk mitigation strategies\n")
        builder.append("6. Recommended portfolio adjustments\n")
        builder.append("7. Confidence score (0-1)\n")

        return builder.toString()
    }

    companion object {
        const val WORK_NAME = "AIAnalysisWorker"
    }
}