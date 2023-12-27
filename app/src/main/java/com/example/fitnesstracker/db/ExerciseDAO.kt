package com.example.fitnesstracker.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercise")
    fun getAll(): List<Exercise>

    @Insert
    fun insert(exercise: Exercise)

    @Query("SELECT SUM(exCount) FROM exercise WHERE exType = :exType")
    fun getTotalCount(exType: String): Int

    @Query("SELECT SUM(exDistance) FROM exercise")
    fun getTotalDistance(): Double
}
