package com.antonkiselev.healthcompanion.data

import androidx.room.TypeConverter
import com.antonkiselev.healthcompanion.model.MetricMode

class HealthTypeConverters {
    @TypeConverter
    fun metricModeToString(value: MetricMode): String = value.name

    @TypeConverter
    fun stringToMetricMode(value: String): MetricMode = MetricMode.valueOf(value)
}

