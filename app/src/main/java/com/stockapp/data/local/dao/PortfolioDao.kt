package com.stockapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stockapp.data.model.Holding
import com.stockapp.data.model.PortfolioSummary
import com.stockapp.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface HoldingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(holding: Holding): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(holdings: List<Holding>)

    @Update
    suspend fun update(holding: Holding): Int

    @Query("DELETE FROM holdings WHERE id = :id")
    suspend fun delete(id: Long): Int

    @Query("DELETE FROM holdings WHERE userId = :userId")
    suspend fun deleteByUserId(userId: Long): Int

    @Query("SELECT * FROM holdings WHERE id = :id")
    suspend fun getById(id: Long): Holding?

    @Query("SELECT * FROM holdings WHERE userId = :userId ORDER BY totalValue DESC")
    fun getByUserId(userId: Long): Flow<List<Holding>>

    @Query("SELECT * FROM holdings WHERE userId = :userId ORDER BY totalValue DESC")
    suspend fun getByUserIdList(userId: Long): List<Holding>

    @Query("SELECT * FROM holdings WHERE userId = :userId AND symbol = :symbol")
    suspend fun getByUserIdAndSymbol(userId: Long, symbol: String): Holding?

    @Query("SELECT * FROM holdings WHERE userId = :userId ORDER BY dayChangePercent DESC LIMIT 5")
    fun getTopGainers(userId: Long): Flow<List<Holding>>

    @Query("SELECT * FROM holdings WHERE userId = :userId ORDER BY dayChangePercent ASC LIMIT 5")
    fun getTopLosers(userId: Long): Flow<List<Holding>>

    @Query("SELECT * FROM holdings WHERE userId = :userId ORDER BY totalGainLoss DESC LIMIT 1")
    suspend fun getTopGainerOverall(userId: Long): Holding?

    @Query("SELECT * FROM holdings WHERE userId = :userId ORDER BY totalGainLoss ASC LIMIT 1")
    suspend fun getTopLoserOverall(userId: Long): Holding?

    @Query("SELECT SUM(totalValue) FROM holdings WHERE userId = :userId")
    suspend fun getTotalValue(userId: Long): Double?

    @Query("SELECT SUM(totalGainLoss) FROM holdings WHERE userId = :userId")
    suspend fun getTotalGainLoss(userId: Long): Double?

    @Query("SELECT COUNT(*) FROM holdings WHERE userId = :userId")
    suspend fun getHoldingCount(userId: Long): Int
}

@Dao
interface PortfolioSummaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(summary: PortfolioSummary): Long

    @Update
    suspend fun update(summary: PortfolioSummary): Int

    @Query("SELECT * FROM portfolio_summary WHERE userId = :userId")
    suspend fun getByUserId(userId: Long): PortfolioSummary?

    @Query("SELECT * FROM portfolio_summary")
    fun getAll(): Flow<List<PortfolioSummary>>
}

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<Transaction>)

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getByUserId(userId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND symbol = :symbol ORDER BY date DESC")
    suspend fun getByUserIdAndSymbol(userId: Long, symbol: String): List<Transaction>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    suspend fun getByDateRange(userId: Long, startDate: String, endDate: String): List<Transaction>
}