package com.stockapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.stockapp.data.repository.IPORepository
import com.stockapp.data.repository.NetworkRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class IPODataSyncWorker @Inject constructor(
    context: Context,
    params: WorkerParameters,
    private val ipoRepository: IPORepository,
    private val networkRepository: NetworkRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Fetch IPO data from multiple sources
            val ipos = networkRepository.fetchIPOData()
            ipoRepository.saveIPOs(ipos)

            // Fetch watchlist IPOs for all users
            val watchlist = ipoRepository.getWatchlist().first()
            for (watchItem in watchlist) {
                try {
                    val updatedIPO = networkRepository.fetchIPODetails(watchItem.symbol)
                    ipoRepository.updateIPO(updatedIPO)
                } catch (e: Exception) {
                    android.util.Log.w("IPODataSyncWorker", "Failed to update watchlist IPO ${watchItem.symbol}", e)
                }
            }

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("IPODataSyncWorker", "Worker failed", e)
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "IPODataSyncWorker"
    }
}