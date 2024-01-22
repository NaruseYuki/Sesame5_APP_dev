package com.yushin.lockapplication

import com.yushin.lockapplication.database.LockDatabase
import android.app.Application
import android.util.Log
import co.candyhouse.sesame.open.CHBleManager
import co.candyhouse.sesame.open.CHConfiguration
import com.yushin.lockapplication.model.LockModel
import com.yushin.lockapplication.repository.LockRepository
import viewModel.LockViewModel

class LockApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("LockApplication", "Application context: $applicationContext")
        val lockDatabase = LockDatabase.getInstance(this) // Ensure you use "this" which refers to the Application context
        val lockRepository = LockRepository(lockDatabase.lockDao())
        LockModel.initialize(lockRepository)
        CHBleManager(this)
        Log.d("test",CHBleManager.mScanning.toString())

    }
}