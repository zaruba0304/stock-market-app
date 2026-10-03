package com.stockapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.stockapp.data.local.converters.DateConverter
import com.stockapp.data.local.converters.DoubleConverter
import com.stockapp.data.local.dao.AIAnalysisDao
import com.stockapp.data.local.dao.BrokerageCredentialsDao
import com.stockapp.data.local.dao.DailyNewsRecapDao
import com.stockapp.data.local.dao.HoldingDao
import com.stockapp.data.local.dao.IPODao
import com.stockapp.data.local.dao.IPOApplicationDao
import com.stockapp.data.local.dao.IPOWatchlistDao
import com.stockapp.data.local.dao.NewsDao
import com.stockapp.data.local.dao.NewsPreferencesDao
import com.stockapp.data.local.dao.PortfolioSummaryDao
import com.stockapp.data.local.dao.ScreenerDao
import com.stockapp.data.local.dao.ScreenerResultDao
import com.stockapp.data.local.dao.SettingsDao
import com.stockapp.data.local.dao.TransactionDao
import com.stockapp.data.local.dao.UserDao
import com.stockapp.data.local.dao.WeeklyPortfolioSummaryDao
import com.stockapp.data.local.dao.WeeklyStockPickDao
import com.stockapp.data.model.AIAnalysis
import com.stockapp.data.model.BrokerageCredentials
import com.stockapp.data.model.DailyNewsRecap
import com.stockapp.data.model.Holding
import com.stockapp.data.model.IPO
import com.stockapp.data.model.IPOApplication
import com.stockapp.data.model.IPOWatchlist
import com.stockapp.data.model.NewsArticle
import com.stockapp.data.model.NewsPreferences
import com.stockapp.data.model.PortfolioSummary
import com.stockapp.data.model.Screener
import com.stockapp.data.model.ScreenerResult
import com.stockapp.data.model.Settings
import com.stockapp.data.model.Transaction
import com.stockapp.data.model.User
import com.stockapp.data.model.WeeklyPortfolioSummary
import com.stockapp.data.model.WeeklyStockPick

@Database(
    entities = [
        User::class,
        Holding::class,
        PortfolioSummary::class,
        Transaction::class,
        IPO::class,
        IPOApplication::class,
        IPOWatchlist::class,
        NewsArticle::class,
        NewsPreferences::class,
        DailyNewsRecap::class,
        WeeklyPortfolioSummary::class,
        Screener::class,
        ScreenerResult::class,
        WeeklyStockPick::class,
        AIAnalysis::class,
        AppSettings::class,
        BrokerageCredentials::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class, DoubleConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun holdingDao(): HoldingDao
    abstract fun portfolioSummaryDao(): PortfolioSummaryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun ipoDao(): IPODao
    abstract fun ipoApplicationDao(): IPOApplicationDao
    abstract fun ipoWatchlistDao(): IPOWatchlistDao
    abstract fun newsDao(): NewsDao
    abstract fun newsPreferencesDao(): NewsPreferencesDao
    abstract fun dailyNewsRecapDao(): DailyNewsRecapDao
    abstract fun weeklyPortfolioSummaryDao(): WeeklyPortfolioSummaryDao
    abstract fun screenerDao(): ScreenerDao
    abstract fun screenerResultDao(): ScreenerResultDao
    abstract fun weeklyStockPickDao(): WeeklyStockPickDao
    abstract fun aiAnalysisDao(): AIAnalysisDao
    abstract fun settingsDao(): SettingsDao
    abstract fun brokerageCredentialsDao(): BrokerageCredentialsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stock_app.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}