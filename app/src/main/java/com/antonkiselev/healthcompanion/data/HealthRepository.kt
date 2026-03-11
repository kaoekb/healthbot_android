package com.antonkiselev.healthcompanion.data

import com.antonkiselev.healthcompanion.model.BloodPressureMeasurement
import com.antonkiselev.healthcompanion.model.SugarMeasurement
import com.antonkiselev.healthcompanion.model.UserPreferences
import java.time.Instant
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HealthRepository(private val dao: HealthDao) {
    fun observePreferences(): Flow<UserPreferences> {
        return dao.observePreferences().map { entity ->
            (entity ?: UserPreferencesEntity()).toDomain()
        }
    }

    fun observeSchedule(): Flow<List<LocalTime>> {
        return dao.observeScheduleSlots().map { slots ->
            slots.map { slot ->
                LocalTime.of(slot.minutesOfDay / 60, slot.minutesOfDay % 60)
            }
        }
    }

    fun observeSugarMeasurements(): Flow<List<SugarMeasurement>> {
        return dao.observeSugarMeasurements().map { rows ->
            rows.map { row ->
                SugarMeasurement(
                    id = row.id,
                    value = row.value,
                    measuredAt = row.measuredAt,
                )
            }
        }
    }

    fun observeBloodPressureMeasurements(): Flow<List<BloodPressureMeasurement>> {
        return dao.observeBloodPressureMeasurements().map { rows ->
            rows.map { row ->
                BloodPressureMeasurement(
                    id = row.id,
                    systolic = row.systolic,
                    diastolic = row.diastolic,
                    pulse = row.pulse,
                    measuredAt = row.measuredAt,
                )
            }
        }
    }

    suspend fun ensureInitialized() {
        if (dao.getPreferencesSnapshot() == null) {
            dao.upsertPreferences(UserPreferencesEntity())
        }
    }

    suspend fun updatePreferences(preferences: UserPreferences) {
        dao.upsertPreferences(preferences.toEntity())
    }

    suspend fun replaceSchedule(times: List<LocalTime>) {
        dao.clearScheduleSlots()
        dao.replaceScheduleSlots(
            times.sorted().map { time ->
                ScheduleSlotEntity(minutesOfDay = time.hour * 60 + time.minute)
            },
        )
    }

    suspend fun addSugar(value: Double, measuredAt: Long) {
        dao.insertSugarMeasurement(
            SugarMeasurementEntity(
                value = value,
                measuredAt = Instant.ofEpochMilli(measuredAt).toEpochMilli(),
            ),
        )
    }

    suspend fun addBloodPressure(
        systolic: Int,
        diastolic: Int,
        pulse: Int,
        measuredAt: Long,
    ) {
        dao.insertBloodPressureMeasurement(
            BloodPressureMeasurementEntity(
                systolic = systolic,
                diastolic = diastolic,
                pulse = pulse,
                measuredAt = Instant.ofEpochMilli(measuredAt).toEpochMilli(),
            ),
        )
    }
}

private fun UserPreferencesEntity.toDomain(): UserPreferences {
    return UserPreferences(
        metricMode = metricMode,
        simultaneous = simultaneous,
        notificationsEnabled = notificationsEnabled,
        timezoneId = timezoneId,
    )
}

private fun UserPreferences.toEntity(): UserPreferencesEntity {
    return UserPreferencesEntity(
        metricMode = metricMode,
        simultaneous = simultaneous,
        notificationsEnabled = notificationsEnabled,
        timezoneId = timezoneId,
    )
}

