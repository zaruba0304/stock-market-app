package com.stockapp.di

import com.stockapp.data.remote.ApiModule
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.Module
import dagger.Provides
import dagger.hilt.android.scopes.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideApiModule(): ApiModule {
        return ApiModule()
    }
}