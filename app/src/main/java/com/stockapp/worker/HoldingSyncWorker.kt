package com.stockapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.stockapp.data.repository.PortfolioRepository
import com.stockapp.data.repository.NetworkRepository
import com.stockapp.data.repository.UserRepository
import com.stockapp.data.model.User
import com.stockapp.data.model.Brokerage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class HoldingSyncWorker @Inject constructor(
    context: Context,
    params: WorkerParameters,
    private val portfolioRepository: PortfolioRepository,
    private val networkRepository: NetworkRepository,
    private val userRepository: UserRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val users = userRepository.getAllUsers().first()
            val activeUsers = users.filter { it.isActive }

            if (activeUsers.isEmpty()) {
                return@withContext Result.success()
            }

            var hasError = false

            for (user in activeUsers) {
                try {
                    syncUserHoldings(user)
                } catch (e: Exception) {
                    hasError = true
                    android.util.Log.e("HoldingSyncWorker", "Failed to sync holdings for user ${user.id}", e)
                }
            }

            if (hasError) {
                Result.retry()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            android.util.Log.e("HoldingSyncWorker", "Worker failed", e)
            Result.retry()
        }
    }

    private suspend fun syncUserHoldings(user: User) {
        user.brokerage?.let { brokerage ->
            val credentials = userRepository.getBrokerageCredentials(user.id!!, brokerage).firstOrNull()
            credentials?.let { creds ->
                when (brokerage) {
                    Brokerage.ZERODHA -> {
                        val holdings = networkRepository.fetchZerodhaHoldings(creds.apiKey, creds.apiSecret, creds.userId)
                        portfolioRepository.saveHoldings(user.id!!, holdings)
                    }
                    Brokerage.GROWW -> {
                        val holdings = networkRepository.fetchGrowwHoldings(creds.apiKey, creds.apiSecret, creds.userId)
                        portfolioRepository.saveHoldings(user.id!!, holdings)
                    }
                    Brokerage.UPSTOX -> {
                        val holdings = networkRepository.fetchUpstoxHoldings(creds.apiKey, creds.apiSecret, creds.userId)
                        portfolioRepository.saveHoldings(user.id!!, holdings)
                    }
                    Brokerage.ANGEL_ONE -> {
                        val holdings = networkRepository.fetchAngelOneHoldings(creds.apiKey, creds.apiSecret, creds.userId)
                        portfolioRepository.saveHoldings(user.id!!, holdings)
                    }
                }
            }
        }
    }

    companion object {
        const val WORK_NAME = "HoldingSyncWorker"
    }
}