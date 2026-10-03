package com.stockapp.di

import com.stockapp.data.repository.IPORepository
import com.stockapp.data.repository.NetworkRepository
import com.stockapp.data.repository.NewsRepository
import com.stockapp.data.repository.PortfolioRepository
import com.stockapp.data.repository.ScreenerRepository
import com.stockapp.data.repository.SettingsRepository
import com.stockapp.data.repository.UserRepository
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.Module
import dagger.Provides
import dagger.hilt.android.scopes.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository(database: com.stockapp.data.local.AppDatabase): UserRepository {
        return UserRepository(database)
    }

    @Provides
    @Singleton
    fun providePortfolioRepository(database: com.stockapp.data.local.AppDatabase): PortfolioRepository {
        return PortfolioRepository(database)
    }

    @Provides
    @Singleton
    fun provideIPORepository(database: com.stockapp.data.local.AppDatabase): IPORepository {
        return IPORepository(database)
    }

    @Provides
    @Singleton
    fun provideNewsRepository(database: com.stockapp.data.local.AppDatabase): NewsRepository {
        return NewsRepository(database)
    }

    @Provides
    @Singleton
    fun provideScreenerRepository(database: com.stockapp.data.local.AppDatabase): ScreenerRepository {
        return ScreenerRepository(database)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(database: com.stockapp.data.local.AppDatabase): SettingsRepository {
        return SettingsRepository(database)
    }

    @Provides
    @Singleton
    fun provideNetworkRepository(
        database: com.stockapp.data.local.AppDatabase,
        apiModule: com.stockapp.data.remote.ApiModule,
        credentialsDao: com.stockapp.data.local.dao.BrokerageCredentialsDao
    ): NetworkRepository {
        return NetworkRepository(database, apiModule, credentialsDao)
    }
}