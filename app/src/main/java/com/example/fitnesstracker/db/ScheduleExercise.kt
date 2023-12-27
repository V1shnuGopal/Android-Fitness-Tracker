package com.example.fitnesstracker.db

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
@Parcelize
data class ScheduleExercise(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val date: Long,
    val time: Long,
    val type: String,
    val reminder: Boolean
) : Parcelable {
}
