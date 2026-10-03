package com.stockapp.di

import com.stockapp.data.local.DatabaseSeeder
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface HiltEntryPoint {
    fun databaseSeeder(): DatabaseSeeder
}