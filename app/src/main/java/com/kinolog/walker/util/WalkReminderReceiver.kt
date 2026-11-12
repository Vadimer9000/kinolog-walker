package com.kinolog.walker.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kinolog.walker.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WalkReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    NotificationHelper.rescheduleAllReminders(context)
                } finally {
                    pendingResult.finish()
                }
            }
        } else if (action == NotificationHelper.ACTION_WALK_REMINDER) {
            val walkId = intent.getLongExtra(NotificationHelper.EXTRA_WALK_ID, -1L)
            if (walkId == -1L) return

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val prefs = PreferencesManager(context)
                    if (!prefs.isNotificationsEnabled) return@launch

                    val db = AppDatabase.getInstance(context)
                    val walk = db.walkEventDao().getWalkByIdSync(walkId)
                    if (walk == null || walk.isCompleted) return@launch

                    val client = db.clientDao().getClientByIdSync(walk.clientId) ?: return@launch
                    NotificationHelper.showNotification(context, client, walk)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
