package com.stockapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stockapp.data.model.IPO
import com.stockapp.data.model.IPOApplication
import com.stockapp.data.model.IPOStatus
import com.stockapp.data.model.IPOWatchlist
import kotlinx.coroutines.flow.Flow

@Dao
interface IPODao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ipo: IPO): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ipos: List<IPO>)

    @Update
    suspend fun update(ipo: IPO): Int

    @Query("DELETE FROM ipos WHERE id = :id")
    suspend fun delete(id: Long): Int

    @Query("SELECT * FROM ipos WHERE id = :id")
    suspend fun getById(id: Long): IPO?

    @Query("SELECT * FROM ipos WHERE symbol = :symbol")
    suspend fun getBySymbol(symbol: String): IPO?

    @Query("SELECT * FROM ipos WHERE status = :status ORDER BY openDate ASC")
    fun getByStatus(status: IPOStatus): Flow<List<IPO>>

    @Query("SELECT * FROM ipos WHERE status IN (:statuses) ORDER BY openDate ASC")
    fun getByStatuses(statuses: List<IPOStatus>): Flow<List<IPO>>

    @Query("SELECT * FROM ipos ORDER BY openDate DESC")
    fun getAll(): Flow<List<IPO>>

    @Query("SELECT * FROM ipos WHERE openDate >= :today ORDER BY openDate ASC")
    fun getUpcoming(today: String): Flow<List<IPO>>

    @Query("SELECT * FROM ipos WHERE status = 'OPEN' ORDER BY closeDate ASC")
    fun getOpen(): Flow<List<IPO>>

    @Query("SELECT * FROM ipos WHERE status = 'ALLOTMENT' ORDER BY listingDate ASC")
    fun getAllotment(): Flow<List<IPO>>

    @Query("SELECT * FROM ipos WHERE gmp IS NOT NULL ORDER BY gmpPercent DESC")
    fun getWithGMP(): Flow<List<IPO>>

    @Query("UPDATE ipos SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: IPOStatus): Int
}

@Dao
interface IPOApplicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(application: IPOApplication): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(applications: List<IPOApplication>)

    @Update
    suspend fun update(application: IPOApplication): Int

    @Query("SELECT * FROM ipo_applications WHERE id = :id")
    suspend fun getById(id: Long): IPOApplication?

    @Query("SELECT * FROM ipo_applications WHERE userId = :userId ORDER BY appliedAt DESC")
    fun getByUserId(userId: Long): Flow<List<IPOApplication>>

    @Query("SELECT * FROM ipo_applications WHERE ipoId = :ipoId ORDER BY appliedAt DESC")
    fun getByIpoId(ipoId: Long): Flow<List<IPOApplication>>

    @Query("SELECT * FROM ipo_applications WHERE userId = :userId AND ipoId = :ipoId")
    suspend fun getByUserAndIpo(userId: Long, ipoId: Long): IPOApplication?

    @Query("SELECT * FROM ipo_applications WHERE ipoId = :ipoId AND allotmentStatus = 'PENDING'")
    fun getPendingAllotment(ipoId: Long): Flow<List<IPOApplication>>

    @Query("SELECT COUNT(*) FROM ipo_applications WHERE userId = :userId")
    suspend fun getApplicationCount(userId: Long): Int
}

@Dao
interface IPOWatchlistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(watchlist: IPOWatchlist): Long

    @Query("DELETE FROM ipo_watchlist WHERE userId = :userId AND ipoId = :ipoId")
    suspend fun remove(userId: Long, ipoId: Long): Int

    @Query("SELECT * FROM ipo_watchlist WHERE userId = :userId")
    fun getByUserId(userId: Long): Flow<List<IPOWatchlist>>

    @Query("SELECT * FROM ipo_watchlist WHERE userId = :userId AND ipoId = :ipoId")
    suspend fun isInWatchlist(userId: Long, ipoId: Long): Boolean
}