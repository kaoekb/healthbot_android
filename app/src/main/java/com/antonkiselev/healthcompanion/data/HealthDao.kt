package com.antonkiselev.healthcompanion.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun observePreferences(): Flow<UserPreferencesEntity?>

    @Query("SELECT * FROM user_preferences WHERE id = 1")
    suspend fun getPreferencesSnapshot(): UserPreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPreferences(entity: UserPreferencesEntity)

    @Query("SELECT * FROM schedule_slots ORDER BY minutes_of_day ASC")
    fun observeScheduleSlots(): Flow<List<ScheduleSlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun replaceScheduleSlots(slots: List<ScheduleSlotEntity>)

    @Query("DELETE FROM schedule_slots")
    suspend fun clearScheduleSlots()

    @Query("SELECT * FROM sugar_measurements ORDER BY measured_at DESC")
    fun observeSugarMeasurements(): Flow<List<SugarMeasurementEntity>>

    @Insert
    suspend fun insertSugarMeasurement(entity: SugarMeasurementEntity)

    @Query("SELECT * FROM blood_pressure_measurements ORDER BY measured_at DESC")
    fun observeBloodPressureMeasurements(): Flow<List<BloodPressureMeasurementEntity>>

    @Insert
    suspend fun insertBloodPressureMeasurement(entity: BloodPressureMeasurementEntity)
}

