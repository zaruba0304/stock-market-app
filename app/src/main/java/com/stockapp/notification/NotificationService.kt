package com.stockapp.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.stockapp.R
import com.stockapp.ui.MainActivity
import com.stockapp.data.model.NewsArticle
import com.stockapp.data.model.ImpactLevel
import com.stockapp.data.model.IPO
import com.stockapp.data.model.IPOStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    private val context: Context
) {

    private val notificationManager = NotificationManagerCompat.from(context)

    private val CHANNEL_NEWS = "news_channel"
    private val CHANNEL_IPO = "ipo_channel"
    private val CHANNEL_PRICE = "price_channel"
    private val CHANNEL_DAILY_RECAP = "daily_recap_channel"
    private val CHANNEL_WEEKLY_SUMMARY = "weekly_summary_channel"

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_NEWS,
                    "News Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Real-time news alerts for your portfolio"
                    enableVibration(true)
                    setShowBadge(true)
                },
                NotificationChannel(
                    CHANNEL_IPO,
                    "IPO Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "IPO opening, allotment, and listing notifications"
                    enableVibration(true)
                    setShowBadge(true)
                },
                NotificationChannel(
                    CHANNEL_PRICE,
                    "Price Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Price movement alerts for your holdings"
                    enableVibration(false)
                    setShowBadge(true)
                },
                NotificationChannel(
                    CHANNEL_DAILY_RECAP,
                    "Daily Recap",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Evening market recap after trading hours"
                    enableVibration(false)
                    setShowBadge(true)
                },
                NotificationChannel(
                    CHANNEL_WEEKLY_SUMMARY,
                    "Weekly Summary",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Weekly portfolio summary and insights"
                    enableVibration(false)
                    setShowBadge(true)
                }
            )

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }

    fun showNewsAlert(article: NewsArticle) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("screen", "news")
            putExtra("article_id", article.id ?: 0)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val priority = when (article.impactLevel) {
            ImpactLevel.CRITICAL -> NotificationCompat.PRIORITY_MAX
            ImpactLevel.HIGH -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_NEWS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(article.title)
            .setContentText(article.summary.take(100))
            .setStyle(NotificationCompat.BigTextStyle().bigText(article.summary))
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_NEWS)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(context.getColor(R.color.primary))
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_read,
                    "Mark Read",
                    getMarkReadPendingIntent(article.id!!)
                ).build()
            )
            .build()

        notificationManager.notify(article.id!!.toInt(), notification)
    }

    fun showIPOAlert(ipo: IPO, alertType: IPOAlertType) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("screen", "ipo")
            putExtra("ipo_id", ipo.id ?: 0)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val (title, message) = when (alertType) {
            IPOAlertType.OPENING -> "IPO Opening: ${ipo.name}" to "${ipo.name} (${ipo.symbol}) opens today for subscription. Price band: ${ipo.formattedPriceBand}"
            IPOAlertType.CLOSING_SOON -> "IPO Closing Soon: ${ipo.name}" to "${ipo.name} closes in ${ipo.daysToClose} day(s). Last chance to apply!"
            IPOAlertType.ALLOTMENT -> "Allotment Status: ${ipo.name}" to "Allotment results are out for ${ipo.name}. Check your status now."
            IPOAlertType.LISTED -> "Listed: ${ipo.name}" to "${ipo.name} is now listed on the exchange. Listing price: ₹${ipo.listingPrice?.let { String.format("%.2f", it) } ?: "N/A"}"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_IPO)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(context.getColor(R.color.primary))
            .build()

        notificationManager.notify(1000 + ipo.id!!.toInt(), notification)
    }

    fun showPriceAlert(symbol: String, currentPrice: Double, changePercent: Double, alertType: PriceAlertType) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("screen", "portfolio")
            putExtra("symbol", symbol)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val (title, message) = when (alertType) {
            PriceAlertType.TARGET_HIT -> "$symbol Target Hit!" to "$symbol reached your target price of ₹${String.format("%.2f", currentPrice)}"
            PriceAlertType.STOP_LOSS -> "$symbol Stop Loss Triggered!" to "$symbol hit your stop loss at ₹${String.format("%.2f", currentPrice)} (${String.format("%.2f", changePercent)}%)"
            PriceAlertType.SIGNIFICANT_MOVE -> "$symbol Significant Move" to "$symbol moved ${String.format("%.2f", changePercent)}% to ₹${String.format("%.2f", currentPrice)}"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_PRICE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(if (changePercent >= 0) context.getColor(R.color.profit) else context.getColor(R.color.loss))
            .build()

        notificationManager.notify(2000 + symbol.hashCode(), notification)
    }

    fun showDailyRecap(recap: com.stockapp.data.model.DailyNewsRecap) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("screen", "news")
            putExtra("show_recap", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_RECAP)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Daily Market Recap")
            .setContentText(recap.summary.take(100))
            .setStyle(NotificationCompat.BigTextStyle().bigText(recap.summary))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SUMMARY)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(context.getColor(R.color.primary))
            .build()

        notificationManager.notify(3000, notification)
    }

    fun showWeeklySummary(summary: com.stockapp.data.model.WeeklyPortfolioSummary) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("screen", "portfolio")
            putExtra("show_weekly_summary", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val weeklyChange = if (summary.weeklyGainLossPercent >= 0) "+${String.format("%.2f", summary.weeklyGainLossPercent)}%" else "${String.format("%.2f", summary.weeklyGainLossPercent)}%"

        val notification = NotificationCompat.Builder(context, CHANNEL_WEEKLY_SUMMARY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Weekly Portfolio Summary")
            .setContentText("Portfolio: ${weeklyChange} this week. ${summary.keyInsights.size} insights.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Weekly Change: $weeklyChange\n" +
                summary.keyInsights.take(3).joinToString("\n")
            ))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SUMMARY)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(context.getColor(R.color.primary))
            .build()

        notificationManager.notify(4000, notification)
    }

    private fun getMarkReadPendingIntent(articleId: Long): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "MARK_READ"
            putExtra("article_id", articleId)
        }
        return PendingIntent.getBroadcast(
            context, articleId.toInt(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun cancelAll() {
        notificationManager.cancelAll()
    }

    enum class IPOAlertType {
        OPENING, CLOSING_SOON, ALLOTMENT, LISTED
    }

    enum class PriceAlertType {
        TARGET_HIT, STOP_LOSS, SIGNIFICANT_MOVE
    }
}