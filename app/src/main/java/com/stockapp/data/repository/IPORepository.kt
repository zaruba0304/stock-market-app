package com.stockapp.data.repository

import com.stockapp.data.local.AppDatabase
import com.stockapp.data.local.dao.IPODao
import com.stockapp.data.local.dao.IPOApplicationDao
import com.stockapp.data.local.dao.IPOWatchlistDao
import com.stockapp.data.model.IPO
import com.stockapp.data.model.IPOApplication
import com.stockapp.data.model.IPOStatus
import com.stockapp.data.model.IPOWatchlist
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IPORepository @Inject constructor(
    private val database: AppDatabase
) {
    private val ipoDao: IPODao = database.ipoDao()
    private val applicationDao: IPOApplicationDao = database.ipoApplicationDao()
    private val watchlistDao: IPOWatchlistDao = database.ipoWatchlistDao()

    // IPOs
    fun getAllIPOs(): Flow<List<IPO>> = ipoDao.getAll()

    fun getIPOsByStatus(status: IPOStatus): Flow<List<IPO>> = ipoDao.getByStatus(status)

    fun getIPOsByStatuses(statuses: List<IPOStatus>): Flow<List<IPO>> = ipoDao.getByStatuses(statuses)

    fun getUpcomingIPOs(today: String): Flow<List<IPO>> = ipoDao.getUpcoming(today)

    fun getOpenIPOs(): Flow<List<IPO>> = ipoDao.getOpen()

    fun getAllotmentIPOs(): Flow<List<IPO>> = ipoDao.getAllotment()

    fun getIPOsWithGMP(): Flow<List<IPO>> = ipoDao.getWithGMP()

    suspend fun getIPOById(id: Long): IPO? = ipoDao.getById(id)

    suspend fun getIPOBySymbol(symbol: String): IPO? = ipoDao.getBySymbol(symbol)

    suspend fun insertIPO(ipo: IPO): Long = ipoDao.insert(ipo)

    suspend fun insertIPOs(ipos: List<IPO>) = ipoDao.insertAll(ipos)

    suspend fun updateIPO(ipo: IPO): Int = ipoDao.update(ipo)

    suspend fun updateIPOStatus(id: Long, status: IPOStatus): Int = ipoDao.updateStatus(id, status)

    suspend fun deleteIPO(id: Long): Int = ipoDao.delete(id)

    // Applications
    fun getApplicationsByUser(userId: Long): Flow<List<IPOApplication>> = applicationDao.getByUserId(userId)

    fun getApplicationsByIPO(ipoId: Long): Flow<List<IPOApplication>> = applicationDao.getByIpoId(ipoId)

    suspend fun getApplication(userId: Long, ipoId: Long): IPOApplication? = applicationDao.getByUserAndIpo(userId, ipoId)

    fun getPendingAllotmentApplications(ipoId: Long): Flow<List<IPOApplication>> = applicationDao.getPendingAllotment(ipoId)

    suspend fun insertApplication(application: IPOApplication): Long = applicationDao.insert(application)

    suspend fun insertApplications(applications: List<IPOApplication>) = applicationDao.insertAll(applications)

    suspend fun updateApplication(application: IPOApplication): Int = applicationDao.update(application)

    suspend fun getApplicationCount(userId: Long): Int = applicationDao.getApplicationCount(userId)

    // Watchlist
    fun getWatchlist(userId: Long): Flow<List<IPOWatchlist>> = watchlistDao.getByUserId(userId)

    suspend fun addToWatchlist(watchlist: IPOWatchlist): Long = watchlistDao.insert(watchlist)

    suspend fun removeFromWatchlist(userId: Long, ipoId: Long): Int = watchlistDao.remove(userId, ipoId)

    suspend fun isInWatchlist(userId: Long, ipoId: Long): Boolean = watchlistDao.isInWatchlist(userId, ipoId)
}