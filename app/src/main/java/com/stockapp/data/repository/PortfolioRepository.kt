package com.stockapp.data.repository

import com.stockapp.data.local.AppDatabase
import com.stockapp.data.local.dao.HoldingDao
import com.stockapp.data.local.dao.PortfolioSummaryDao
import com.stockapp.data.local.dao.TransactionDao
import com.stockapp.data.model.Holding
import com.stockapp.data.model.PortfolioSummary
import com.stockapp.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PortfolioRepository @Inject constructor(
    private val database: AppDatabase
) {
    private val holdingDao: HoldingDao = database.holdingDao()
    private val summaryDao: PortfolioSummaryDao = database.portfolioSummaryDao()
    private val transactionDao: TransactionDao = database.transactionDao()

    // Holdings
    fun getHoldings(userId: Long): Flow<List<Holding>> = holdingDao.getByUserId(userId)

    suspend fun getHoldingsList(userId: Long): List<Holding> = holdingDao.getByUserIdList(userId)

    suspend fun getHolding(userId: Long, symbol: String): Holding? = holdingDao.getByUserIdAndSymbol(userId, symbol)

    fun getTopGainers(userId: Long): Flow<List<Holding>> = holdingDao.getTopGainers(userId)

    fun getTopLosers(userId: Long): Flow<List<Holding>> = holdingDao.getTopLosers(userId)

    suspend fun getTopGainerOverall(userId: Long): Holding? = holdingDao.getTopGainerOverall(userId)

    suspend fun getTopLoserOverall(userId: Long): Holding? = holdingDao.getTopLoserOverall(userId)

    suspend fun getTotalValue(userId: Long): Double = holdingDao.getTotalValue(userId) ?: 0.0

    suspend fun getTotalGainLoss(userId: Long): Double = holdingDao.getTotalGainLoss(userId) ?: 0.0

    suspend fun getHoldingCount(userId: Long): Int = holdingDao.getHoldingCount(userId)

    suspend fun insertHolding(holding: Holding): Long = holdingDao.insert(holding)

    suspend fun insertHoldings(holdings: List<Holding>) = holdingDao.insertAll(holdings)

    suspend fun updateHolding(holding: Holding): Int = holdingDao.update(holding)

    suspend fun deleteHolding(id: Long): Int = holdingDao.delete(id)

    suspend fun deleteUserHoldings(userId: Long): Int = holdingDao.deleteByUserId(userId)

    // Portfolio Summary
    suspend fun getPortfolioSummary(userId: Long): PortfolioSummary? = summaryDao.getByUserId(userId)

    fun getAllPortfolioSummaries(): Flow<List<PortfolioSummary>> = summaryDao.getAll()

    suspend fun insertOrUpdateSummary(summary: PortfolioSummary) = summaryDao.insert(summary)

    // Transactions
    fun getTransactions(userId: Long): Flow<List<Transaction>> = transactionDao.getByUserId(userId)

    suspend fun getTransactionsBySymbol(userId: Long, symbol: String): List<Transaction> =
        transactionDao.getByUserIdAndSymbol(userId, symbol)

    suspend fun getTransactionsByDateRange(userId: Long, startDate: String, endDate: String): List<Transaction> =
        transactionDao.getByDateRange(userId, startDate, endDate)

    suspend fun insertTransaction(transaction: Transaction): Long = transactionDao.insert(transaction)

    suspend fun insertTransactions(transactions: List<Transaction>) = transactionDao.insertAll(transactions)
}