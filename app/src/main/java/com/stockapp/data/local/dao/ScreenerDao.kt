package com.stockapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stockapp.data.model.AIAnalysis
import com.stockapp.data.model.AIAnalysisType
import com.stockapp.data.model.Screener
import com.stockapp.data.model.ScreenerResult
import com.stockapp.data.model.WeeklyStockPick
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreenerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(screener: Screener): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(screeners: List<Screener>)

    @Update
    suspend fun update(screener: Screener): Int

    @Query("DELETE FROM screeners WHERE id = :id")
    suspend fun delete(id: Long): Int

    @Query("SELECT * FROM screeners WHERE id = :id")
    suspend fun getById(id: Long): Screener?

    @Query("SELECT * FROM screeners WHERE category = :category AND isActive = 1 ORDER BY isDefault DESC, name ASC")
    fun getByCategory(category: String): Flow<List<Screener>>

    @Query("SELECT * FROM screeners WHERE isActive = 1 ORDER BY isDefault DESC, category, name ASC")
    fun getAllActive(): Flow<List<Screener>>

    @Query("SELECT * FROM screeners WHERE isDefault = 1 AND isActive = 1")
    fun getDefaults(): Flow<List<Screener>>
}

@Dao
interface ScreenerResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: ScreenerResult): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(results: List<ScreenerResult>)

    @Query("DELETE FROM screener_results WHERE screenerId = :screenerId")
    suspend fun deleteByScreenerId(screenerId: Long): Int

    @Query("DELETE FROM screener_results WHERE scannedAt < :cutoffDate")
    suspend fun deleteOldResults(cutoffDate: String): Int

    @Query("SELECT * FROM screener_results WHERE screenerId = :screenerId ORDER BY score DESC")
    fun getByScreenerId(screenerId: Long): Flow<List<ScreenerResult>>

    @Query("SELECT * FROM screener_results WHERE screenerId = :screenerId ORDER BY score DESC LIMIT :limit")
    suspend fun getTopByScreenerId(screenerId: Long, limit: Int): List<ScreenerResult>

    @Query("SELECT * FROM screener_results WHERE scannedAt >= :date ORDER BY score DESC")
    fun getByDate(date: String): Flow<List<ScreenerResult>>
}

@Dao
interface WeeklyStockPickDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pick: WeeklyStockPick): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(picks: List<WeeklyStockPick>)

    @Query("DELETE FROM weekly_stock_picks WHERE weekStartDate = :weekStart")
    suspend fun deleteByWeek(weekStart: String): Int

    @Query("SELECT * FROM weekly_stock_picks WHERE weekStartDate = :weekStart ORDER BY confidence DESC")
    fun getByWeek(weekStart: String): Flow<List<WeeklyStockPick>>

    @Query("SELECT * FROM weekly_stock_picks WHERE weekStartDate >= :startDate ORDER BY weekStartDate DESC, confidence DESC")
    fun getRecent(startDate: String): Flow<List<WeeklyStockPick>>
}

@Dao
interface AIAnalysisDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(analysis: AIAnalysis): Long

    @Query("SELECT * FROM ai_analysis WHERE type = :type AND symbol = :symbol ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestByTypeAndSymbol(type: AIAnalysisType, symbol: String): AIAnalysis?

    @Query("SELECT * FROM ai_analysis WHERE userId = :userId AND type = :type ORDER BY createdAt DESC LIMIT :limit")
    fun getByUserAndType(userId: Long, type: AIAnalysisType, limit: Int): Flow<List<AIAnalysis>>

    @Query("SELECT * FROM ai_analysis WHERE type = :type ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getByType(type: AIAnalysisType, limit: Int): List<AIAnalysis>

    @Query("DELETE FROM ai_analysis WHERE createdAt < :cutoffDate")
    suspend fun deleteOld(cutoffDate: String): Int
}