package com.stockapp.data.repository

import com.stockapp.data.local.AppDatabase
import com.stockapp.data.local.dao.UserDao
import com.stockapp.data.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val database: AppDatabase
) {
    private val userDao: UserDao = database.userDao()

    fun getAllUsers(): Flow<List<User>> = userDao.getAll()

    suspend fun getAllUsersList(): List<User> = userDao.getAllList()

    suspend fun getUserById(id: Long): User? = userDao.getById(id)

    suspend fun getPrimaryUser(): User? = userDao.getPrimaryUser()

    suspend fun getUserByPan(pan: String): User? = userDao.getByPan(pan)

    fun getUsersByBrokerage(brokerageId: String): Flow<List<User>> = userDao.getByBrokerage(brokerageId)

    suspend fun insertUser(user: User): Long = userDao.insert(user)

    suspend fun insertUsers(users: List<User>) = userDao.insertAll(users)

    suspend fun updateUser(user: User): Int = userDao.update(user)

    suspend fun deleteUser(id: Long): Int = userDao.delete(id)

    suspend fun setPrimaryUser(id: Long) {
        userDao.clearPrimary()
        userDao.setPrimary(id)
    }
}