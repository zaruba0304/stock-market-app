package com.stockapp.data.repository

import com.stockapp.data.local.AppDatabase
import com.stockapp.data.local.dao.AIAnalysisDao
import com.stockapp.data.local.dao.ScreenerDao
import com.stockapp.data.local.dao.ScreenerResultDao
import com.stockapp.data.local.dao.WeeklyStockPickDao
import com.stockapp.data.model.AIAnalysis
import com.stockapp.data.model.AIAnalysisType
import com.stockapp.data.model.Screener
import com.stockapp.data.model.ScreenerResult
import com.stockapp.data.model.WeeklyStockPick
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenerRepository @Inject constructor(
    private val database: AppDatabase
) {
    private val screenerDao: ScreenerDao = database.screenerDao()
    private val resultDao: ScreenerResultDao = database.screenerResultDao()
    private val pickDao: WeeklyStockPickDao = database.weeklyStockPickDao()
    private val aiDao: AIAnalysisDao = database.aiAnalysisDao()

    // Screeners
    fun getActiveScreeners(): Flow<List<Screener>> = screenerDao.getAllActive()

    fun getScreenersByCategory(category: String): Flow<List<Screener>> = screenerDao.getByCategory(category)

    fun getDefaultScreeners(): Flow<List<Screener>> = screenerDao.getDefaults()

    suspend fun getScreenerById(id: Long): Screener? = screenerDao.getById(id)

    suspend fun insertScreener(screener: Screener): Long = screenerDao.insert(screener)

    suspend fun insertScreeners(screeners: List<Screener>) = screenerDao.insertAll(screeners)

    suspend fun updateScreener(screener: Screener): Int = screenerDao.update(screener)

    suspend fun deleteScreener(id: Long): Int = screenerDao.delete(id)

    // Screener Results
    fun getResultsByScreener(screenerId: Long): Flow<List<ScreenerResult>> = resultDao.getByScreenerId(screenerId)

    suspend fun getTopResultsByScreener(screenerId: Long, limit: Int): List<ScreenerResult> =
        resultDao.getTopByScreenerId(screenerId, limit)

    fun getResultsByDate(date: String): Flow<List<ScreenerResult>> = resultDao.getByDate(date)

    suspend fun insertResult(result: ScreenerResult): Long = resultDao.insert(result)

    suspend fun insertResults(results: List<ScreenerResult>) = resultDao.insertAll(results)

    suspend fun deleteResultsByScreener(screenerId: Long): Int = resultDao.deleteByScreenerId(screenerId)

    suspend fun deleteOldResults(cutoffDate: String): Int = resultDao.deleteOldResults(cutoffDate)

    // Weekly Stock Picks
    fun getPicksByWeek(weekStart: String): Flow<List<WeeklyStockPick>> = pickDao.getByWeek(weekStart)

    fun getRecentPicks(startDate: String): Flow<List<WeeklyStockPick>> = pickDao.getRecent(startDate)

    suspend fun insertPick(pick: WeeklyStockPick): Long = pickDao.insert(pick)

    suspend fun insertPicks(picks: List<WeeklyStockPick>) = pickDao.insertAll(picks)

    suspend fun deletePicksByWeek(weekStart: String): Int = pickDao.deleteByWeek(weekStart)

    // AI Analysis
    suspend fun getLatestAnalysis(type: AIAnalysisType, symbol: String): AIAnalysis? =
        aiDao.getLatestByTypeAndSymbol(type, symbol)

    fun getAnalysesByUserAndType(userId: Long, type: AIAnalysisType, limit: Int): Flow<List<AIAnalysis>> =
        aiDao.getByUserAndType(userId, type, limit)

    suspend fun getAnalysesByType(type: AIAnalysisType, limit: Int): List<AIAnalysis> =
        aiDao.getByType(type, limit)

    suspend fun insertAnalysis(analysis: AIAnalysis): Long = aiDao.insert(analysis)

    suspend fun deleteOldAnalyses(cutoffDate: String): Int = aiDao.deleteOld(cutoffDate)
}