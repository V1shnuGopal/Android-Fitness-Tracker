package com.example.fitnesstracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstracker.R
import com.example.fitnesstracker.db.Exercise
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PastExerciseAdapter(private val itemCL: (Exercise) -> Unit) : ListAdapter<Exercise, PastExerciseAdapter.ExerciseViewHolder>(ExerciseDiffCallback()) {

    class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.exDate)
        val typeTextView: TextView = itemView.findViewById(R.id.exType)

        fun bind(exercise: Exercise, clickListener: (Exercise) -> Unit) {
            itemView.setOnClickListener { clickListener(exercise) }
        }
    }

    class ExerciseDiffCallback : DiffUtil.ItemCallback<Exercise>() {
        override fun areItemsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_layout, parent, false)
        return ExerciseViewHolder(view)
    }
    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = getItem(position)

        val date = Date(exercise.exDate)

        // Create a SimpleDateFormat object and format the Date object to a string.
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val dateString = dateFormat.format(date)

        holder.dateTextView.text = dateString
        holder.typeTextView.text = exercise.exType

        holder.bind(exercise, itemCL)

    }
}