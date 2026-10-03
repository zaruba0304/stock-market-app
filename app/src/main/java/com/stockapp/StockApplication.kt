package com.stockapp

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.EntryPointAccessors
import com.stockapp.data.local.DatabaseSeeder
import com.stockapp.di.HiltEntryPoint

@HiltAndroidApp
class StockApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize database seeder
        val entryPoint = EntryPointAccessors.fromApplication(this, HiltEntryPoint::class.java)
        val seeder = entryPoint.databaseSeeder()
        ProcessLifecycleOwner.get().lifecycle.addObserver(seeder)
    }
}