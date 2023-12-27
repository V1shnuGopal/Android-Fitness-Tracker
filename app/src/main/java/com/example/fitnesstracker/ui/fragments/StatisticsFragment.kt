package com.example.fitnesstracker.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.example.fitnesstracker.R
import com.example.fitnesstracker.db.AppDatabase
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StatisticsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val spinner: Spinner = view.findViewById(R.id.chartSelect)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.exercise_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter

            // Set initial selection to 'Run'
            val position = adapter.getPosition("Average Speed Per Run")
            spinner.setSelection(position)
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val selected = if (parent.getItemAtPosition(pos).toString() == "Average Speed Per Run"){
                    "Run"
                } else{
                    "Walk"
                }
                setupBarChart(selected)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }
    }

    private fun setupBarChart(exType: String) {
        val barChart = view?.findViewById<BarChart>(R.id.chart)
        val totalStepsTextView = view?.findViewById<TextView>(R.id.totalSteps)
        val totalDistanceTextView = view?.findViewById<TextView>(R.id.totalDistance)
        val totalTimeTextView = view?.findViewById<TextView>(R.id.totalTime)
        val avgSpeedTextView = view?.findViewById<TextView>(R.id.avgSpeed)


        val xAxis = barChart?.xAxis
        xAxis?.setDrawGridLines(false)
        xAxis?.setDrawAxisLine(true)
        xAxis?.position = XAxis.XAxisPosition.BOTTOM
        xAxis?.setDrawLabels(false)

        val yAxisLeft = barChart?.axisLeft
        yAxisLeft?.setDrawGridLines(false)
        yAxisLeft?.axisMinimum = 0f

        val yAxisRight = barChart?.axisRight
        yAxisRight?.setDrawGridLines(false)
        yAxisRight?.axisMinimum = 0f

        CoroutineScope(Dispatchers.IO).launch {
            val db = Room.databaseBuilder(
                requireActivity().applicationContext,
                AppDatabase::class.java, AppDatabase.ExerciseHistoryDB
            ).build()

            val totalSteps = db.exerciseDao().getTotalCount("Walk")
            val totalDistance = db.exerciseDao().getTotalDistance()
            val totalTime = db.exerciseDao().getTotalCount("Run")
            val avgSpeed = if (totalTime != 0) totalDistance / totalTime else 0.0

            val totalDistanceInKM = totalDistance / 1000

            val hours = totalTime / (60 * 60 * 100)
            val minutes = (totalTime % (60 * 60 * 100)) / (60 * 100)
            val seconds = (totalTime % (60 * 100)) / 100
            val centiSeconds = totalTime % 100

            val timeString = "${String.format("%02d", hours)}:${String.format("%02d", minutes)}:${String.format("%02d", seconds)}:${String.format("%02d", centiSeconds)}"

            val exercises = db.exerciseDao().getAll()

            val barEntries = ArrayList<BarEntry>()

            // Filter the exercises to include only those with 'exType' as selected
            val filteredExercises = exercises.filter { it.exType == exType }

            for ((index, exercise) in filteredExercises.withIndex()) {
                val value = when (exType) {
                    "Run" -> {
                        // Calculate speed in km/h for 'Run' exercises
                        (exercise.exDistance/1000) / (exercise.exCount.toFloat()/360000)
                    }
                    "Walk" -> {
                        // Use step count for 'Walk' exercises
                        exercise.exCount.toFloat()
                    }
                    else -> 0f
                }
                barEntries.add(BarEntry(index.toFloat(), value))
            }

            withContext(Dispatchers.Main) {
                val label = when (exType) {
                    "Run" -> "Speed"
                    "Walk" -> "Step Count"
                    else -> ""
                }

                totalStepsTextView?.text = totalSteps.toString()
                totalDistanceTextView?.text = getString(R.string.kilometers, totalDistanceInKM)
                totalTimeTextView?.text = timeString
                avgSpeedTextView?.text = getString(R.string.kilometers_per_hour, avgSpeed)

                val dataSet = BarDataSet(barEntries, label)
                val data = BarData(dataSet)
                barChart?.description?.isEnabled = false
                barChart?.data = data
                barChart?.invalidate() // Refresh the chart
            }
        }
    }
}
