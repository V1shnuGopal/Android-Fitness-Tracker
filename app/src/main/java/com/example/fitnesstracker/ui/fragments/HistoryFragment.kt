package com.example.fitnesstracker.ui.fragments

import android.app.AlertDialog
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.fitnesstracker.R
import com.example.fitnesstracker.adapters.PastExerciseAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import com.example.fitnesstracker.db.AppDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: PastExerciseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById<RecyclerView>(R.id.exHistory).apply {
            layoutManager = LinearLayoutManager(requireContext())
            viewAdapter = PastExerciseAdapter { exercise ->
                // Your existing click listener code goes here

                val date = Date(exercise.exDate)
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val dateString = dateFormat.format(date)

                val details = when (exercise.exType) {
                    "Run" -> {
                        val totalTime = exercise.exCount // assuming exCount is the total time in milliseconds
                        val hours = totalTime / (60 * 60 * 100)
                        val minutes = (totalTime % (60 * 60 * 100)) / (60 * 100)
                        val seconds = (totalTime % (60 * 100)) / 100
                        val centiSeconds = totalTime % 100
                        val timeString = "${String.format("%02d", hours)}:${String.format("%02d", minutes)}:${String.format("%02d", seconds)}:${String.format("%02d", centiSeconds)}"
                        "Time: $timeString\nDistance: ${exercise.exDistance}"
                    }
                    "Walk" -> {
                        // assuming exCount is the step count for 'Walk' exercises
                        "Step Count: ${exercise.exCount}\nDistance: ${exercise.exDistance}"
                    }
                    else -> ""
                }

                AlertDialog.Builder(requireContext())
                    .setTitle("Exercise Details")
                    .setMessage("Date: $dateString\nType: ${exercise.exType}\n$details")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
            adapter = viewAdapter
        }

        updateData()
    }

    private fun updateData() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = Room.databaseBuilder(
                requireContext(),
                AppDatabase::class.java, AppDatabase.ExerciseHistoryDB
            ).build()

            val exercises = db.exerciseDao().getAll()

            withContext(Dispatchers.Main) {
                viewAdapter.submitList(exercises)
            }
        }
    }
}