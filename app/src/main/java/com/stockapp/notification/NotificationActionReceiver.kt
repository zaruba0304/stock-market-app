package com.stockapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import com.stockapp.data.repository.NewsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var newsRepository: NewsRepository

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val articleId = intent.getLongExtra("article_id", -1)

        when (action) {
            "MARK_READ" -> {
                if (articleId != -1L) {
                    // Use WorkManager to mark as read in background
                    WorkManager.getInstance(context).enqueue(
                        androidx.work.OneTimeWorkRequestBuilder<MarkReadWorker>()
                            .setInputData(androidx.work.Data.Builder().putLong("article_id", articleId).build())
                            .build()
                    )
                }
            }
        }
    }
}