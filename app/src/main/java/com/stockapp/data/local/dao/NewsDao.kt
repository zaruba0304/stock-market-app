package com.stockapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stockapp.data.model.DailyNewsRecap
import com.stockapp.data.model.NewsArticle
import com.stockapp.data.model.NewsPreferences
import com.stockapp.data.model.WeeklyPortfolioSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(article: NewsArticle): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<NewsArticle>)

    @Update
    suspend fun update(article: NewsArticle): Int

    @Query("DELETE FROM news WHERE id = :id")
    suspend fun delete(id: Long): Int

    @Query("DELETE FROM news WHERE publishedAt < :cutoffDate")
    suspend fun deleteOldArticles(cutoffDate: String): Int

    @Query("SELECT * FROM news WHERE id = :id")
    suspend fun getById(id: Long): NewsArticle?

    @Query("SELECT * FROM news WHERE :symbol IN (SELECT value FROM json_each(symbols)) ORDER BY publishedAt DESC LIMIT :limit")
    fun getBySymbol(symbol: String, limit: Int): Flow<List<NewsArticle>>

    @Query("SELECT * FROM news WHERE symbols LIKE '%' || :symbol || '%' ORDER BY publishedAt DESC LIMIT :limit")
    suspend fun getBySymbolList(symbol: String, limit: Int): List<NewsArticle>

    @Query("SELECT * FROM news WHERE impactScore >= :minScore ORDER BY impactScore DESC, publishedAt DESC LIMIT :limit")
    fun getHighImpact(minScore: Double, limit: Int): Flow<List<NewsArticle>>

    @Query("SELECT * FROM news WHERE isRead = 0 ORDER BY publishedAt DESC LIMIT :limit")
    fun getUnread(limit: Int): Flow<List<NewsArticle>>

    @Query("SELECT * FROM news WHERE isBookmarked = 1 ORDER BY publishedAt DESC")
    fun getBookmarked(): Flow<List<NewsArticle>>

    @Query("SELECT * FROM news ORDER BY publishedAt DESC LIMIT :limit")
    fun getLatest(limit: Int): Flow<List<NewsArticle>>

    @Query("SELECT * FROM news WHERE date(publishedAt) = date(:date) ORDER BY impactScore DESC")
    fun getByDate(date: String): Flow<List<NewsArticle>>

    @Query("UPDATE news SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long): Int

    @Query("UPDATE news SET isRead = 1 WHERE id IN (:ids)")
    suspend fun markMultipleAsRead(ids: List<Long>): Int

    @Query("UPDATE news SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun setBookmarked(id: Long, bookmarked: Boolean): Int

    @Query("SELECT COUNT(*) FROM news WHERE isRead = 0")
    suspend fun getUnreadCount(): Int
}

@Dao
interface NewsPreferencesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preferences: NewsPreferences): Long

    @Update
    suspend fun update(preferences: NewsPreferences): Int

    @Query("SELECT * FROM news_preferences WHERE userId = :userId")
    suspend fun getByUserId(userId: Long): NewsPreferences?

    @Query("SELECT * FROM news_preferences")
    fun getAll(): Flow<List<NewsPreferences>>
}

@Dao
interface DailyNewsRecapDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recap: DailyNewsRecap): Long

    @Query("SELECT * FROM daily_news_recap WHERE date = :date")
    suspend fun getByDate(date: String): DailyNewsRecap?

    @Query("SELECT * FROM daily_news_recap ORDER BY date DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<DailyNewsRecap>>
}

@Dao
interface WeeklyPortfolioSummaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(summary: WeeklyPortfolioSummary): Long

    @Query("SELECT * FROM weekly_portfolio_summary WHERE userId = :userId AND weekStartDate = :weekStart")
    suspend fun getByUserAndWeek(userId: Long, weekStart: String): WeeklyPortfolioSummary?

    @Query("SELECT * FROM weekly_portfolio_summary WHERE userId = :userId ORDER BY weekStartDate DESC LIMIT :limit")
    fun getByUserId(userId: Long, limit: Int): Flow<List<WeeklyPortfolioSummary>>

    @Query("SELECT * FROM weekly_portfolio_summary WHERE weekStartDate = :weekStart")
    fun getByWeek(weekStart: String): Flow<List<WeeklyPortfolioSummary>>
}