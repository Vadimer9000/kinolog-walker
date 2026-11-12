package com.kinolog.walker

import android.app.Application
import com.kinolog.walker.data.AppDatabase
import com.kinolog.walker.util.GeocoderHelper
import com.kinolog.walker.util.NotificationHelper
import com.kinolog.walker.util.PreferencesManager

class KinologApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Pre-initialize Room database instance
        AppDatabase.getInstance(this)

        // Pre-create notification channel
        NotificationHelper.createNotificationChannel(this)

        val prefs = PreferencesManager(this)
        GeocoderHelper.init(this, prefs.yandexApiKey)
    }
}
