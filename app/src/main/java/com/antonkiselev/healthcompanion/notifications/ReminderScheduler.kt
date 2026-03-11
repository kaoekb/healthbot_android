package com.antonkiselev.healthcompanion.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.antonkiselev.healthcompanion.model.UserPreferences
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object ReminderScheduler {
    private const val REMINDER_TAG = "health_compass_reminder"

    fun sync(context: Context, schedule: List<LocalTime>, preferences: UserPreferences) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(REMINDER_TAG)

        if (!preferences.notificationsEnabled || schedule.isEmpty()) {
            return
        }

        schedule.distinct().forEach { time ->
            enqueue(context, time, preferences.timezoneId)
        }
    }

    fun enqueue(context: Context, time: LocalTime, timezoneId: String) {
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(calculateDelay(time, timezoneId))
            .addTag(REMINDER_TAG)
            .setInputData(
                workDataOf(
                    ReminderWorker.KEY_HOUR to time.hour,
                    ReminderWorker.KEY_MINUTE to time.minute,
                    ReminderWorker.KEY_TIMEZONE_ID to timezoneId,
                ),
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueName(time),
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    private fun uniqueName(time: LocalTime): String {
        return "health_compass_reminder_${time.hour}_${time.minute}"
    }

    private fun calculateDelay(time: LocalTime, timezoneId: String): Duration {
        val zoneId = runCatching { ZoneId.of(timezoneId) }.getOrDefault(ZoneId.systemDefault())
        val now = ZonedDateTime.now(zoneId)
        var nextReminder = now
            .withHour(time.hour)
            .withMinute(time.minute)
            .withSecond(0)
            .withNano(0)

        if (!nextReminder.isAfter(now)) {
            nextReminder = nextReminder.plusDays(1)
        }

        return Duration.between(now, nextReminder)
    }
}

