package com.stockapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.stockapp.data.repository.NewsRepository
import com.stockapp.data.repository.NetworkRepository
import com.stockapp.data.repository.PortfolioRepository
import com.stockapp.data.model.NewsArticle
import com.stockapp.data.model.ImpactLevel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class NewsFetchWorker @Inject constructor(
    context: Context,
    params: WorkerParameters,
    private val newsRepository: NewsRepository,
    private val networkRepository: NetworkRepository,
    private val portfolioRepository: PortfolioRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Get all portfolio symbols across all users
            val allHoldings = portfolioRepository.getAllHoldings().first()
            val symbols = allHoldings.map { it.symbol }.distinct()

            if (symbols.isEmpty()) {
                return@withContext Result.success()
            }

            // Fetch news for portfolio symbols
            val articles = networkRepository.fetchNewsForSymbols(symbols)

            // Analyze and score each article
            val analyzedArticles = articles.map { article ->
                analyzeArticle(article, symbols)
            }

            // Save articles
            newsRepository.saveArticles(analyzedArticles)

            // Check for high-impact news and trigger notifications
            val highImpactArticles = analyzedArticles.filter { it.impactLevel == ImpactLevel.HIGH || it.impactLevel == ImpactLevel.CRITICAL }
            if (highImpactArticles.isNotEmpty()) {
                triggerNotifications(highImpactArticles)
            }

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("NewsFetchWorker", "Worker failed", e)
            Result.retry()
        }
    }

    private fun analyzeArticle(article: NewsArticle, portfolioSymbols: List<String>): NewsArticle {
        var impactLevel = ImpactLevel.LOW
        var sentiment = com.stockapp.data.model.NewsSentiment.NEUTRAL

        // Simple keyword-based analysis
        val content = "${article.title} ${article.summary}".lowercase()
        val portfolioMentions = portfolioSymbols.count { symbol -> content.contains(symbol.lowercase()) }

        // Determine impact based on portfolio mentions and keywords
        val highImpactKeywords = listOf("earnings", "results", "dividend", "bonus", "split", "merger", "acquisition", "buyback", "guidance", "forecast", "downgrade", "upgrade", "target", "rating")
        val negativeKeywords = listOf("loss", "decline", "fall", "drop", "crash", "bearish", "downgrade", "sell", "risk", "concern", "warning")
        val positiveKeywords = listOf("profit", "growth", "rise", "surge", "bullish", "upgrade", "buy", "strong", "beat", "exceed", "record")

        val hasHighImpactKeyword = highImpactKeywords.any { content.contains(it) }
        val negativeCount = negativeKeywords.count { content.contains(it) }
        val positiveCount = positiveKeywords.count { content.contains(it) }

        // Impact level
        when {
            portfolioMentions >= 3 || (portfolioMentions >= 1 && hasHighImpactKeyword) -> impactLevel = ImpactLevel.CRITICAL
            portfolioMentions >= 2 || hasHighImpactKeyword -> impactLevel = ImpactLevel.HIGH
            portfolioMentions >= 1 -> impactLevel = ImpactLevel.MEDIUM
            else -> impactLevel = ImpactLevel.LOW
        }

        // Sentiment
        when {
            positiveCount > negativeCount -> sentiment = com.stockapp.data.model.NewsSentiment.POSITIVE
            negativeCount > positiveCount -> sentiment = com.stockapp.data.model.NewsSentiment.NEGATIVE
            else -> sentiment = com.stockapp.data.model.NewsSentiment.NEUTRAL
        }

        return article.copy(
            impactLevel = impactLevel,
            sentiment = sentiment,
            symbols = portfolioSymbols.filter { content.contains(it.lowercase()) }
        )
    }

    private fun triggerNotifications(articles: List<NewsArticle>) {
        // Notification logic would go here
        // For now, just log
        android.util.Log.i("NewsFetchWorker", "High impact articles found: ${articles.size}")
    }

    companion object {
        const val WORK_NAME = "NewsFetchWorker"
    }
}