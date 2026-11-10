package com.kinolog.walker

import android.app.Application
import com.kinolog.walker.data.AppDatabase

class KinologApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Pre-initialize Room database instance
        AppDatabase.getInstance(this)
    }
}
