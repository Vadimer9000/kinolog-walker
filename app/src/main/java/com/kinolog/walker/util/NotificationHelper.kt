package com.kinolog.walker.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.kinolog.walker.MainActivity
import com.kinolog.walker.R
import com.kinolog.walker.data.AppDatabase
import com.kinolog.walker.model.ClientEntity
import com.kinolog.walker.model.PetItem
import com.kinolog.walker.model.WalkEventEntity
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object NotificationHelper {

    const val CHANNEL_ID = "walk_reminders"
    const val CHANNEL_NAME = "Напоминания о записях"
    const val ACTION_WALK_REMINDER = "com.kinolog.walker.ACTION_WALK_REMINDER"
    const val EXTRA_WALK_ID = "walk_id"
    const val EXTRA_CLIENT_ID = "client_id"

    private val json = Json { ignoreUnknownKeys = true }

    fun createNotificationChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Напоминания о предстоящих записях и выгулах"
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun scheduleReminder(context: Context, client: ClientEntity, walk: WalkEventEntity) {
        val prefs = PreferencesManager(context)
        if (!prefs.isNotificationsEnabled || walk.isCompleted) {
            cancelReminder(context, walk.id)
            return
        }

        val date = LocalDate.ofEpochDay(walk.dateEpochDay)
        val hours = walk.startTimeMinutes / 60
        val mins = walk.startTimeMinutes % 60
        val walkDateTime = LocalDateTime.of(date, LocalTime.of(hours, mins))
        val leadTimeMinutes = prefs.notificationLeadTimeMinutes
        val reminderDateTime = walkDateTime.minusMinutes(leadTimeMinutes.toLong())
        val triggerAtMillis = reminderDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (triggerAtMillis <= System.currentTimeMillis()) {
            // Already in past
            return
        }

        val intent = Intent(context, WalkReminderReceiver::class.java).apply {
            action = ACTION_WALK_REMINDER
            putExtra(EXTRA_WALK_ID, walk.id)
            putExtra(EXTRA_CLIENT_ID, walk.clientId)
        }
        val requestCode = walk.id.toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancelReminder(context: Context, walkId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WalkReminderReceiver::class.java).apply {
            action = ACTION_WALK_REMINDER
        }
        val requestCode = walkId.toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(requestCode)
    }

    suspend fun rescheduleAllReminders(context: Context) {
        val prefs = PreferencesManager(context)
        val db = AppDatabase.getInstance(context)
        val todayEpochDay = LocalDate.now().toEpochDay()
        val upcomingWalks = db.walkEventDao().getUpcomingUncompletedWalks(todayEpochDay)

        if (!prefs.isNotificationsEnabled) {
            for (walk in upcomingWalks) {
                cancelReminder(context, walk.id)
            }
            return
        }

        val clients = db.clientDao().getAllClientsList().associateBy { it.id }
        for (walk in upcomingWalks) {
            val client = clients[walk.clientId]
            if (client != null) {
                scheduleReminder(context, client, walk)
            }
        }
    }

    fun showNotification(context: Context, client: ClientEntity, walk: WalkEventEntity) {
        val prefs = PreferencesManager(context)
        if (!prefs.isNotificationsEnabled || walk.isCompleted) return

        createNotificationChannel(context)

        val petNames = try {
            if (client.petsJson.isNotBlank()) {
                val pets = json.decodeFromString<List<PetItem>>(client.petsJson)
                pets.map { it.name.trim() }.filter { it.isNotBlank() }.joinToString(", ").ifBlank { "Питомец" }
            } else {
                "Питомец"
            }
        } catch (_: Exception) {
            "Питомец"
        }

        val title = "Скоро ${walk.category.title}: $petNames (${client.fullName})"
        val timeFormatted = String.format("%02d:%02d", walk.startTimeMinutes / 60, walk.startTimeMinutes % 60)

        val fullAddress = buildString {
            append(client.addressStreet.trim())
            if (client.entrance.isNotBlank()) append(", под. ${client.entrance.trim()}")
            if (client.floor.isNotBlank()) append(", эт. ${client.floor.trim()}")
            if (client.apartment.isNotBlank()) append(", кв. ${client.apartment.trim()}")
        }
        val text = "Начало в $timeFormatted • $fullAddress"

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val requestCode = walk.id.toInt()
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(requestCode, notification)
    }
}
