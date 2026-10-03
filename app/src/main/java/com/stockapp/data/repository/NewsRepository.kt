package com.stockapp.data.repository

import com.stockapp.data.local.AppDatabase
import com.stockapp.data.local.dao.DailyNewsRecapDao
import com.stockapp.data.local.dao.NewsDao
import com.stockapp.data.local.dao.NewsPreferencesDao
import com.stockapp.data.local.dao.WeeklyPortfolioSummaryDao
import com.stockapp.data.model.DailyNewsRecap
import com.stockapp.data.model.NewsArticle
import com.stockapp.data.model.NewsPreferences
import com.stockapp.data.model.WeeklyPortfolioSummary
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val database: AppDatabase
) {
    private val newsDao: NewsDao = database.newsDao()
    private val preferencesDao: NewsPreferencesDao = database.newsPreferencesDao()
    private val recapDao: DailyNewsRecapDao = database.dailyNewsRecapDao()
    private val weeklySummaryDao: WeeklyPortfolioSummaryDao = database.weeklyPortfolioSummaryDao()

    // News Articles
    fun getLatestNews(limit: Int = 50): Flow<List<NewsArticle>> = newsDao.getLatest(limit)

    fun getHighImpactNews(minScore: Double = 60.0, limit: Int = 20): Flow<List<NewsArticle>> =
        newsDao.getHighImpact(minScore, limit)

    fun getUnreadNews(limit: Int = 50): Flow<List<NewsArticle>> = newsDao.getUnread(limit)

    fun getBookmarkedNews(): Flow<List<NewsArticle>> = newsDao.getBookmarked()

    fun getNewsBySymbol(symbol: String, limit: Int = 20): Flow<List<NewsArticle>> =
        newsDao.getBySymbol(symbol, limit)

    suspend fun getNewsBySymbolList(symbol: String, limit: Int = 20): List<NewsArticle> =
        newsDao.getBySymbolList(symbol, limit)

    fun getNewsByDate(date: String): Flow<List<NewsArticle>> = newsDao.getByDate(date)

    suspend fun getNewsById(id: Long): NewsArticle? = newsDao.getById(id)

    suspend fun insertNews(article: NewsArticle): Long = newsDao.insert(article)

    suspend fun insertNewsList(articles: List<NewsArticle>) = newsDao.insertAll(articles)

    suspend fun updateNews(article: NewsArticle): Int = newsDao.update(article)

    suspend fun markAsRead(id: Long): Int = newsDao.markAsRead(id)

    suspend fun markMultipleAsRead(ids: List<Long>): Int = newsDao.markMultipleAsRead(ids)

    suspend fun setBookmarked(id: Long, bookmarked: Boolean): Int = newsDao.setBookmarked(id, bookmarked)

    suspend fun getUnreadCount(): Int = newsDao.getUnreadCount()

    suspend fun deleteOldArticles(cutoffDate: String): Int = newsDao.deleteOldArticles(cutoffDate)

    // Preferences
    suspend fun getPreferences(userId: Long): NewsPreferences? = preferencesDao.getByUserId(userId)

    fun getAllPreferences(): Flow<List<NewsPreferences>> = preferencesDao.getAll()

    suspend fun insertOrUpdatePreferences(preferences: NewsPreferences) = preferencesDao.insert(preferences)

    // Daily Recap
    suspend fun getDailyRecap(date: String): DailyNewsRecap? = recapDao.getByDate(date)

    fun getRecentRecaps(limit: Int = 30): Flow<List<DailyNewsRecap>> = recapDao.getRecent(limit)

    suspend fun insertDailyRecap(recap: DailyNewsRecap): Long = recapDao.insert(recap)

    // Weekly Summary
    suspend fun getWeeklySummary(userId: Long, weekStart: String): WeeklyPortfolioSummary? =
        weeklySummaryDao.getByUserAndWeek(userId, weekStart)

    fun getWeeklySummaries(userId: Long, limit: Int = 10): Flow<List<WeeklyPortfolioSummary>> =
        weeklySummaryDao.getByUserId(userId, limit)

    fun getWeeklySummariesByWeek(weekStart: String): Flow<List<WeeklyPortfolioSummary>> =
        weeklySummaryDao.getByWeek(weekStart)

    suspend fun insertWeeklySummary(summary: WeeklyPortfolioSummary): Long = weeklySummaryDao.insert(summary)
}