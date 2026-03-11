package com.antonkiselev.healthcompanion.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.antonkiselev.healthcompanion.model.MetricMode

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "metric_mode") val metricMode: MetricMode = MetricMode.BOTH,
    @ColumnInfo(name = "simultaneous") val simultaneous: Boolean = true,
    @ColumnInfo(name = "notifications_enabled") val notificationsEnabled: Boolean = true,
    @ColumnInfo(name = "timezone_id") val timezoneId: String = java.time.ZoneId.systemDefault().id,
)

@Entity(tableName = "schedule_slots")
data class ScheduleSlotEntity(
    @PrimaryKey
    @ColumnInfo(name = "minutes_of_day")
    val minutesOfDay: Int,
)

@Entity(tableName = "sugar_measurements")
data class SugarMeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "value") val value: Double,
    @ColumnInfo(name = "measured_at") val measuredAt: Long,
)

@Entity(tableName = "blood_pressure_measurements")
data class BloodPressureMeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "systolic") val systolic: Int,
    @ColumnInfo(name = "diastolic") val diastolic: Int,
    @ColumnInfo(name = "pulse") val pulse: Int,
    @ColumnInfo(name = "measured_at") val measuredAt: Long,
)

