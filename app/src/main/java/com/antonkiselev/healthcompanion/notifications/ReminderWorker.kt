package com.antonkiselev.healthcompanion.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.antonkiselev.healthcompanion.MainActivity
import com.antonkiselev.healthcompanion.R
import com.antonkiselev.healthcompanion.data.HealthDatabase
import com.antonkiselev.healthcompanion.data.UserPreferencesEntity
import com.antonkiselev.healthcompanion.model.MetricMode
import java.time.LocalTime

class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val hour = inputData.getInt(KEY_HOUR, -1)
        val minute = inputData.getInt(KEY_MINUTE, -1)
        val timezoneId = inputData.getString(KEY_TIMEZONE_ID).orEmpty()

        if (hour !in 0..23 || minute !in 0..59) {
            return Result.failure()
        }

        val preferences = HealthDatabase.getInstance(applicationContext)
            .healthDao()
            .getPreferencesSnapshot() ?: UserPreferencesEntity()

        if (preferences.notificationsEnabled) {
            createNotificationChannel(applicationContext)
            showReminderNotification(
                context = applicationContext,
                preferences = preferences,
                time = LocalTime.of(hour, minute),
            )
        }

        ReminderScheduler.enqueue(
            context = applicationContext,
            time = LocalTime.of(hour, minute),
            timezoneId = timezoneId.ifBlank { preferences.timezoneId },
        )

        return Result.success()
    }

    @SuppressLint("MissingPermission")
    private fun showReminderNotification(
        context: Context,
        preferences: UserPreferencesEntity,
        time: LocalTime,
    ) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val launchIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.health_popeye_logo_1)
            .setContentTitle("Пора на замер")
            .setContentText(buildReminderText(preferences))
            .setStyle(NotificationCompat.BigTextStyle().bigText(buildReminderText(preferences)))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSubText(time.toString())
            .build()

        NotificationManagerCompat.from(context).notify(time.hashCode(), notification)
    }

    private fun buildReminderText(preferences: UserPreferencesEntity): String {
        return when (preferences.metricMode) {
            MetricMode.SUGAR -> "Проверьте сахар и зафиксируйте результат."
            MetricMode.BLOOD_PRESSURE -> "Измерьте давление и пульс."
            MetricMode.BOTH -> {
                if (preferences.simultaneous) {
                    "Время для сахара и давления одним заходом."
                } else {
                    "Время для очередного слота измерений: сахар и давление по очереди."
                }
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Health reminders",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Ежедневные напоминания об измерениях"
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val KEY_HOUR = "hour"
        const val KEY_MINUTE = "minute"
        const val KEY_TIMEZONE_ID = "timezone_id"
        private const val CHANNEL_ID = "health_compass_reminders"
    }
}
