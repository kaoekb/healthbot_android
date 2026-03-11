package com.antonkiselev.healthcompanion.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UserPreferencesEntity::class,
        ScheduleSlotEntity::class,
        SugarMeasurementEntity::class,
        BloodPressureMeasurementEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(HealthTypeConverters::class)
abstract class HealthDatabase : RoomDatabase() {
    abstract fun healthDao(): HealthDao

    companion object {
        @Volatile
        private var instance: HealthDatabase? = null

        fun getInstance(context: Context): HealthDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    HealthDatabase::class.java,
                    "health_compass.db",
                ).build().also { instance = it }
            }
        }
    }
}

