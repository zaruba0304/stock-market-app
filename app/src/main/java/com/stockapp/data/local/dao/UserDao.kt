package com.stockapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stockapp.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<User>)

    @Update
    suspend fun update(user: User): Int

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun delete(id: Long): Int

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: Long): User?

    @Query("SELECT * FROM users ORDER BY isPrimary DESC, name ASC")
    fun getAll(): Flow<List<User>>

    @Query("SELECT * FROM users ORDER BY isPrimary DESC, name ASC")
    suspend fun getAllList(): List<User>

    @Query("SELECT * FROM users WHERE isPrimary = 1")
    suspend fun getPrimaryUser(): User?

    @Query("SELECT * FROM users WHERE panNumber = :pan")
    suspend fun getByPan(pan: String): User?

    @Query("SELECT * FROM users WHERE brokerageId = :brokerageId")
    fun getByBrokerage(brokerageId: String): Flow<List<User>>

    @Query("UPDATE users SET isPrimary = 0")
    suspend fun clearPrimary()

    @Query("UPDATE users SET isPrimary = 1 WHERE id = :id")
    suspend fun setPrimary(id: Long): Int
}