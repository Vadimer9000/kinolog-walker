package com.kinolog.walker.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kinolog.walker.MainActivity
import com.kinolog.walker.model.ClientEntity
import com.kinolog.walker.model.WalkEventEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class WalkNotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "kinolog_walks_reminder_channel"
        const val KEY_TITLE = "title"
        const val KEY_MESSAGE = "message"
        const val KEY_WALK_ID = "walk_id"

        fun scheduleReminder(
            context: Context,
            client: ClientEntity,
            walk: WalkEventEntity
        ) {
            val date = LocalDate.ofEpochDay(walk.dateEpochDay)
            val hours = walk.startTimeMinutes / 60
            val mins = walk.startTimeMinutes % 60
            val walkTime = LocalDateTime.of(date, LocalTime.of(hours, mins))
            val reminderTime = walkTime.minusMinutes(30)
            val now = LocalDateTime.now()

            val delayMinutes = ChronoUnit.MINUTES.between(now, reminderTime)
            if (delayMinutes < 0) {
                return // Event is in the past or less than 30 mins away
            }

            val fullAddress = buildString {
                append(client.addressStreet)
                if (client.entrance.isNotBlank()) append(", под. ${client.entrance}")
                if (client.floor.isNotBlank()) append(", эт. ${client.floor}")
                if (client.apartment.isNotBlank()) append(", кв. ${client.apartment}")
            }

            val timeFormatted = String.format("%02d:%02d", hours, mins)
            val title = "${walk.category.title} в $timeFormatted"
            val message = buildString {
                append("Клиент: ${client.fullName}\n")
                append("Адрес: $fullAddress\n")
                if (walk.walkComment.isNotBlank()) {
                    append("Заметка: ${walk.walkComment}")
                }
            }

            val inputData = Data.Builder()
                .putString(KEY_TITLE, title)
                .putString(KEY_MESSAGE, message)
                .putLong(KEY_WALK_ID, walk.id)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<WalkNotificationWorker>()
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                .setInputData(inputData)
                .addTag("walk_${walk.id}")
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }

        fun cancelReminder(context: Context, walkId: Long) {
            WorkManager.getInstance(context).cancelAllWorkByTag("walk_$walkId")
        }
    }

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: "Напоминание о выгуле"
        val message = inputData.getString(KEY_MESSAGE) ?: "Скоро запланирован выгул"
        val walkId = inputData.getLong(KEY_WALK_ID, 0L)

        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            walkId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(walkId.toInt(), notification)

        return Result.success()
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Выгулы и дрессировки"
            val descriptionText = "Напоминания о предстоящих выгулах питомцев"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
