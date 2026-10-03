package com.stockapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.stockapp.worker.HoldingSyncWorker
import com.stockapp.worker.IPODataSyncWorker
import com.stockapp.worker.NewsFetchWorker
import com.stockapp.worker.DailyRecapWorker
import com.stockapp.worker.WeeklySummaryWorker
import com.stockapp.worker.ScreenerWorker
import com.stockapp.worker.AIAnalysisWorker
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {

            schedulePeriodicWorks(context)
        }
    }

    private fun schedulePeriodicWorks(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // Holding sync - every 30 minutes during market hours
        val holdingSyncRequest = PeriodicWorkRequest.Builder(HoldingSyncWorker::class.java, 30, TimeUnit.MINUTES)
            .setInitialDelay(5, TimeUnit.MINUTES)
            .addTag("holding_sync")
            .build()
        workManager.enqueueUniquePeriodicWork(
            "holding_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            holdingSyncRequest
        )

        // IPO data sync - every 2 hours
        val ipoSyncRequest = PeriodicWorkRequest.Builder(IPODataSyncWorker::class.java, 2, TimeUnit.HOURS)
            .setInitialDelay(10, TimeUnit.MINUTES)
            .addTag("ipo_sync")
            .build()
        workManager.enqueueUniquePeriodicWork(
            "ipo_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            ipoSyncRequest
        )

        // News fetch - every 15 minutes during market hours
        val newsFetchRequest = PeriodicWorkRequest.Builder(NewsFetchWorker::class.java, 15, TimeUnit.MINUTES)
            .setInitialDelay(2, TimeUnit.MINUTES)
            .addTag("news_fetch")
            .build()
        workManager.enqueueUniquePeriodicWork(
            "news_fetch",
            ExistingPeriodicWorkPolicy.KEEP,
            newsFetchRequest
        )

        // Daily recap - every day at 4:30 PM (after market close)
        val dailyRecapRequest = PeriodicWorkRequest.Builder(DailyRecapWorker::class.java, 24, TimeUnit.HOURS)
            .setInitialDelay(calculateInitialDelayForTime(16, 30), TimeUnit.MINUTES)
            .addTag("daily_recap")
            .build()
        workManager.enqueueUniquePeriodicWork(
            "daily_recap",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyRecapRequest
        )

        // Weekly summary - every Sunday at 10 AM
        val weeklySummaryRequest = PeriodicWorkRequest.Builder(WeeklySummaryWorker::class.java, 7, TimeUnit.DAYS)
            .setInitialDelay(calculateInitialDelayForDayOfWeek(1, 10, 0), TimeUnit.MINUTES) // Sunday = 1
            .addTag("weekly_summary")
            .build()
        workManager.enqueueUniquePeriodicWork(
            "weekly_summary",
            ExistingPeriodicWorkPolicy.KEEP,
            weeklySummaryRequest
        )

        // Screener - every Saturday at 9 AM
        val screenerRequest = PeriodicWorkRequest.Builder(ScreenerWorker::class.java, 7, TimeUnit.DAYS)
            .setInitialDelay(calculateInitialDelayForDayOfWeek(7, 9, 0), TimeUnit.MINUTES) // Saturday = 7
            .addTag("screener")
            .build()
        workManager.enqueueUniquePeriodicWork(
            "screener",
            ExistingPeriodicWorkPolicy.KEEP,
            screenerRequest
        )

        // AI Analysis - every day at 6 AM
        val aiAnalysisRequest = PeriodicWorkRequest.Builder(AIAnalysisWorker::class.java, 24, TimeUnit.HOURS)
            .setInitialDelay(calculateInitialDelayForTime(6, 0), TimeUnit.MINUTES)
            .addTag("ai_analysis")
            .build()
        workManager.enqueueUniquePeriodicWork(
            "ai_analysis",
            ExistingPeriodicWorkPolicy.KEEP,
            aiAnalysisRequest
        )
    }

    private fun calculateInitialDelayForTime(hour: Int, minute: Int): Long {
        val now = java.util.Calendar.getInstance()
        val target = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        if (target.timeInMillis <= now.timeInMillis) {
            target.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }

        return (target.timeInMillis - now.timeInMillis) / (1000 * 60)
    }

    private fun calculateInitialDelayForDayOfWeek(dayOfWeek: Int, hour: Int, minute: Int): Long {
        val now = java.util.Calendar.getInstance()
        val target = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.DAY_OF_WEEK, dayOfWeek)
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        // If target day already passed this week, move to next week
        if (target.timeInMillis <= now.timeInMillis) {
            target.add(java.util.Calendar.WEEK_OF_YEAR, 1)
        }

        return (target.timeInMillis - now.timeInMillis) / (1000 * 60)
    }
}