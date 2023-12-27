package com.example.fitnesstracker.ui.fragments

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.MainActivity
import com.google.android.gms.location.LocationServices
import com.mikhaellopez.circularprogressbar.CircularProgressBar

class WalkingFragment : TrackingFragment(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepDetector: Sensor? = null
    private var stepCount = 0

    private lateinit var circularProgressBar: CircularProgressBar
    private var stepCounterView: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_walking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        circularProgressBar = view.findViewById(R.id.circularProgressBar)
        val bottomNavigationView = (activity as MainActivity).bottomNavigationView

        val startButton = view.findViewById<Button>(R.id.startW)
        val finishButton = view.findViewById<Button>(R.id.finishW)

        val currentDate = Calendar.getInstance().timeInMillis

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        startButton.setOnClickListener {
            // Reset the step count when the start button is pressed.
            startTracking()
            (activity as MainActivity).modeSwitch.isEnabled = false
            for (i in 0 until bottomNavigationView.menu.size()) {
                bottomNavigationView.menu.getItem(i).isEnabled = false
            }
            sensorManager.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_UI)
            startButton.visibility = View.GONE
            finishButton.visibility = View.VISIBLE
        }

        finishButton.setOnClickListener {
            sensorManager.unregisterListener(this)
            startButton.visibility = View.VISIBLE
            finishButton.visibility = View.GONE

            for (i in 0 until bottomNavigationView.menu.size()) {
                bottomNavigationView.menu.getItem(i).isEnabled = true
            }
            (activity as MainActivity).modeSwitch.isEnabled = true

            stopTracking()
            insertIntoDB(currentDate, "Walk", stepCount.toLong())

            stepCount = 0
            stepCounterView?.text = "0"
            circularProgressBar.progress = 0f
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
            // Increment the step count each time a step is detected.
            stepCount++

            // Update UI here with the new step count.
            stepCounterView = view?.findViewById(R.id.stepCount)
            stepCounterView?.text = stepCount.toString()

            circularProgressBar.progress = stepCount.toFloat()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the sensor listener when the fragment is destroyed.
        sensorManager.unregisterListener(this)
    }
}
