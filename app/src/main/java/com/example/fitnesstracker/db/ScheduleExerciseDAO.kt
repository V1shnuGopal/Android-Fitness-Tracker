package com.example.fitnesstracker.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ScheduleExerciseDao {
    @Query("SELECT * FROM scheduleExercise")
    fun getAll(): List<ScheduleExercise>

    @Insert
    fun insert(exercise: ScheduleExercise)

    @Delete
    fun delete(schExercise: ScheduleExercise)

    @Update
    fun update(scheduledExercise: ScheduleExercise)
}