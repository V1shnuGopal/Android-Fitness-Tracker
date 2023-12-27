package com.example.fitnesstracker.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val exDate: Long,
    val exType: String,
    val exCount: Long,
    val exDistance: Float
)
