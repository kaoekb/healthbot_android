package com.antonkiselev.healthcompanion.model

enum class MetricMode(val label: String) {
    SUGAR("Сахар"),
    BLOOD_PRESSURE("Давление"),
    BOTH("Оба показателя"),
}

data class UserPreferences(
    val metricMode: MetricMode = MetricMode.BOTH,
    val simultaneous: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val timezoneId: String = java.time.ZoneId.systemDefault().id,
)

data class SugarMeasurement(
    val id: Long,
    val value: Double,
    val measuredAt: Long,
)

data class BloodPressureMeasurement(
    val id: Long,
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int,
    val measuredAt: Long,
)

