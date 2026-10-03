package com.stockapp.di

import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.stockapp.worker.HoldingSyncWorker
import com.stockapp.worker.IPODataSyncWorker
import com.stockapp.worker.NewsFetchWorker
import com.stockapp.worker.DailyRecapWorker
import com.stockapp.worker.WeeklySummaryWorker
import com.stockapp.worker.ScreenerWorker
import com.stockapp.worker.AIAnalysisWorker
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.Module
import dagger.Provides
import dagger.hilt.android.scopes.Singleton
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    @Provides
    @Singleton
    fun provideWorkerFactory(
        holdingSyncWorker: HoldingSyncWorker,
        ipoDataSyncWorker: IPODataSyncWorker,
        newsFetchWorker: NewsFetchWorker,
        dailyRecapWorker: DailyRecapWorker,
        weeklySummaryWorker: WeeklySummaryWorker,
        screenerWorker: ScreenerWorker,
        aiAnalysisWorker: AIAnalysisWorker
    ): HiltWorkerFactory {
        return HiltWorkerFactory()
    }
}

// Worker classes need to be annotated with @HiltWorker
// This is just the module for providing the factory