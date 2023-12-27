package com.example.fitnesstracker.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.fitnesstracker.R
import com.example.fitnesstracker.adapters.ScheduleExerciseAdapter
import com.example.fitnesstracker.db.AppDatabase
import com.example.fitnesstracker.ui.ScheduleActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScheduleFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: ScheduleExerciseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_schedule, container, false)

        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(activity, ScheduleActivity::class.java)
            startActivity(intent)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById<RecyclerView>(R.id.exSchedule).apply {
            layoutManager = LinearLayoutManager(requireContext())
            viewAdapter = ScheduleExerciseAdapter()
            adapter = viewAdapter
        }

        updateData()
    }

    override fun onResume() {
        super.onResume()

        updateData()
    }

    private fun updateData() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = Room.databaseBuilder(
                requireContext(),
                AppDatabase::class.java, AppDatabase.ScheduleExerciseDB
            ).build()

            val scheduledExercises = db.scheduledExerciseDao().getAll()

            withContext(Dispatchers.Main) {
                viewAdapter.updateData(scheduledExercises)
            }
        }
    }
}