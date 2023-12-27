package com.example.fitnesstracker.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Exercise::class, ScheduleExercise::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun scheduledExerciseDao(): ScheduleExerciseDao

    companion object {
        const val ExerciseHistoryDB = "ExerciseHistory"
        const val ScheduleExerciseDB = "ScheduledExercises"
    }
}
