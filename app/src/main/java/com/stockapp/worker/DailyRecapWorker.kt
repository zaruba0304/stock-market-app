package com.stockapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.stockapp.data.repository.NewsRepository
import com.stockapp.data.repository.PortfolioRepository
import com.stockapp.data.model.DailyNewsRecap
import com.stockapp.data.model.ImpactLevel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class DailyRecapWorker @Inject constructor(
    context: Context,
    params: WorkerParameters,
    private val newsRepository: NewsRepository,
    private val portfolioRepository: PortfolioRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val today = LocalDate.now()
            val startOfDay = today.atStartOfDay()
            val endOfDay = today.plusDays(1).atStartOfDay()

            // Get today's articles
            val articles = newsRepository.getArticlesByDateRange(startOfDay, endOfDay).first()

            if (articles.isEmpty()) {
                return@withContext Result.success()
            }

            // Get portfolio summary
            val portfolioSummary = portfolioRepository.getPortfolioSummary().first()

            // Categorize articles
            val criticalArticles = articles.filter { it.impactLevel == ImpactLevel.CRITICAL }
            val highImpactArticles = articles.filter { it.impactLevel == ImpactLevel.HIGH }
            val portfolioArticles = articles.filter { it.symbols.isNotEmpty() }
            val positiveArticles = articles.filter { it.sentiment == com.stockapp.data.model.NewsSentiment.POSITIVE }
            val negativeArticles = articles.filter { it.sentiment == com.stockapp.data.model.NewsSentiment.NEGATIVE }

            // Generate summary
            val summary = generateSummary(
                articles = articles,
                criticalArticles = criticalArticles,
                highImpactArticles = highImpactArticles,
                portfolioArticles = portfolioArticles,
                positiveArticles = positiveArticles,
                negativeArticles = negativeArticles,
                portfolioSummary = portfolioSummary
            )

            // Determine portfolio impact
            val portfolioImpact = when {
                criticalArticles.isNotEmpty() -> "HIGH"
                highImpactArticles.size >= 3 -> "MODERATE"
                portfolioArticles.isNotEmpty() -> "LOW"
                else -> "MINIMAL"
            }

            // Create recap
            val recap = DailyNewsRecap(
                date = today,
                summary = summary,
                articlesCount = articles.size,
                criticalNewsCount = criticalArticles.size,
                highImpactCount = highImpactArticles.size,
                portfolioImpact = portfolioImpact,
                topPositiveNews = positiveArticles.take(3).map { it.title },
                topNegativeNews = negativeArticles.take(3).map { it.title },
                keySymbols = portfolioArticles.flatMap { it.symbols }.distinct().take(10),
                generatedAt = LocalDateTime.now()
            )

            newsRepository.saveDailyRecap(recap)

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("DailyRecapWorker", "Worker failed", e)
            Result.retry()
        }
    }

    private fun generateSummary(
        articles: List<com.stockapp.data.model.NewsArticle>,
        criticalArticles: List<com.stockapp.data.model.NewsArticle>,
        highImpactArticles: List<com.stockapp.data.model.NewsArticle>,
        portfolioArticles: List<com.stockapp.data.model.NewsArticle>,
        positiveArticles: List<com.stockapp.data.model.NewsArticle>,
        negativeArticles: List<com.stockapp.data.model.NewsArticle>,
        portfolioSummary: com.stockapp.data.model.PortfolioSummary?
    ): String {
        val builder = StringBuilder()

        builder.append("Market Recap for ${LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}\n\n")

        builder.append("📊 Portfolio: ")
        portfolioSummary?.let { summary ->
            builder.append("₹${String.format("%.2f", summary.totalValue)} ")
            builder.append(if (summary.dayChange >= 0) "▲" else "▼")
            builder.append(" ${if (summary.dayChange >= 0) "+" else ""}₹${String.format("%.2f", summary.dayChange)} (${if (summary.dayChangePercent >= 0) "+" else ""}${String.format("%.2f", summary.dayChangePercent)}%)\n\n")
        } ?: builder.append("Data not available\n\n")

        builder.append("📰 ${articles.size} news articles analyzed\n")

        if (criticalArticles.isNotEmpty()) {
            builder.append("🔴 ${criticalArticles.size} CRITICAL alerts\n")
        }
        if (highImpactArticles.isNotEmpty()) {
            builder.append("🟠 ${highImpactArticles.size} HIGH impact news\n")
        }

        builder.append("\n📈 Top Positive: ")
        if (positiveArticles.isNotEmpty()) {
            builder.append(positiveArticles.take(2).joinToString("; ") { it.title.take(50) })
        } else {
            builder.append("None")
        }

        builder.append("\n📉 Top Negative: ")
        if (negativeArticles.isNotEmpty()) {
            builder.append(negativeArticles.take(2).joinToString("; ") { it.title.take(50) })
        } else {
            builder.append("None")
        }

        if (portfolioArticles.isNotEmpty()) {
            builder.append("\n\n🎯 Portfolio stocks in news: ${portfolioArticles.flatMap { it.symbols }.distinct().joinToString(", ")}")
        }

        return builder.toString()
    }

    companion object {
        const val WORK_NAME = "DailyRecapWorker"
    }
}