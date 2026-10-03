package com.stockapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stockapp.data.model.AppSettings
import com.stockapp.data.model.BrokerageCredentials
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: AppSettings): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(settings: List<AppSettings>)

    @Update
    suspend fun update(setting: AppSettings): Int

    @Query("SELECT * FROM app_settings WHERE key = :key")
    suspend fun getByKey(key: String): AppSettings?

    @Query("SELECT * FROM app_settings")
    fun getAll(): Flow<List<AppSettings>>

    @Query("SELECT value FROM app_settings WHERE key = :key")
    suspend fun getValue(key: String): String?

    @Query("DELETE FROM app_settings WHERE key = :key")
    suspend fun delete(key: String): Int
}

@Dao
interface BrokerageCredentialsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(credentials: BrokerageCredentials): Long

    @Update
    suspend fun update(credentials: BrokerageCredentials): Int

    @Query("SELECT * FROM brokerage_credentials WHERE id = :id")
    suspend fun getById(id: Long): BrokerageCredentials?

    @Query("SELECT * FROM brokerage_credentials WHERE userId = :userId AND brokerageId = :brokerageId")
    suspend fun getByUserAndBrokerage(userId: Long, brokerageId: String): BrokerageCredentials?

    @Query("SELECT * FROM brokerage_credentials WHERE userId = :userId AND isActive = 1")
    fun getActiveByUserId(userId: Long): Flow<List<BrokerageCredentials>>

    @Query("SELECT * FROM brokerage_credentials WHERE userId = :userId")
    suspend fun getAllByUserId(userId: Long): List<BrokerageCredentials>

    @Query("UPDATE brokerage_credentials SET isActive = 0 WHERE userId = :userId AND brokerageId = :brokerageId")
    suspend fun deactivate(userId: Long, brokerageId: String): Int
}