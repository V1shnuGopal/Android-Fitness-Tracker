package com.example.fitnesstracker.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.fitnesstracker.R
import com.example.fitnesstracker.db.AppDatabase
import com.example.fitnesstracker.db.ScheduleExercise
import com.example.fitnesstracker.ui.ScheduleActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScheduleExerciseAdapter : ListAdapter<ScheduleExercise, ScheduleExerciseAdapter.ScheduleExerciseViewHolder>(ScheduleExerciseDiffCallback()) {

    class ScheduleExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.schDate)
        val timeTextView: TextView = itemView.findViewById(R.id.schTime)
        val iconImageView: ImageView = itemView.findViewById(R.id.schType)
        val schDelete: ImageView = itemView.findViewById(R.id.schDelete)
        val schEdit: ImageView = itemView.findViewById(R.id.schEdit)
    }

    class ScheduleExerciseDiffCallback : DiffUtil.ItemCallback<ScheduleExercise>() {
        override fun areItemsTheSame(oldItem: ScheduleExercise, newItem: ScheduleExercise): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ScheduleExercise, newItem: ScheduleExercise): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleExerciseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.schedule_layout, parent, false)
        return ScheduleExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleExerciseViewHolder, position: Int) {
        val schExercise = getItem(position)

        val date = Date(schExercise.date)
        val time = Date(schExercise.time)

        // Create a SimpleDateFormat object and format the Date object to a string.
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateString = dateFormat.format(date)
        val timeString = timeFormat.format(time)


        holder.dateTextView.text = dateString
        holder.timeTextView.text = timeString
        // Set other views as needed

        if (schExercise.type == "Run") {
            holder.iconImageView.setImageResource(R.drawable.ic_run)
        } else if (schExercise.type == "Walk") {
            holder.iconImageView.setImageResource(R.drawable.ic_walk)
        }

        holder.schDelete.setOnClickListener {
            // Get the scheduled exercise to be deleted.
            val schExerciseToDelete = getItem(holder.adapterPosition)

            CoroutineScope(Dispatchers.IO).launch {
                val db = Room.databaseBuilder(
                    holder.itemView.context,
                    AppDatabase::class.java, AppDatabase.ScheduleExerciseDB
                ).build()

                // Delete the scheduled exercise from the database.
                db.scheduledExerciseDao().delete(schExerciseToDelete)

                // Fetch the updated list from the database.
                val updatedList = db.scheduledExerciseDao().getAll()

                withContext(Dispatchers.Main) {
                    // Update the adapter's data with the new list.
                    submitList(updatedList)
                }
            }
        }

        holder.schEdit.setOnClickListener {
            val schExerciseToEdit = getItem(holder.adapterPosition)

            // Create an Intent to start ScheduleActivity.
            val intent = Intent(holder.itemView.context, ScheduleActivity::class.java)

            // Pass the data of the selected item to ScheduleActivity.
            intent.putExtra("schExercise", schExerciseToEdit)

            holder.itemView.context.startActivity(intent)
        }
    }

    fun updateData(newData: List<ScheduleExercise>) {
        submitList(newData)  // This replaces your existing data and refreshes the RecyclerView
    }
}
