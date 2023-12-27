package com.example.fitnesstracker.ui.fragments

import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.MainActivity
import com.google.android.gms.location.LocationServices

class RunningFragment : TrackingFragment() {

    private var startTime = 0L
    private var timeInMilliseconds = 0L
    private var timeSwapBuff = 0L
    private var updatedTime = 0L
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timer: Runnable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_running, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val startButton = view.findViewById<Button>(R.id.startR)
        val finishButton = view.findViewById<Button>(R.id.finishR)
        val timerTextView = view.findViewById<TextView>(R.id.timerCount)
        val bottomNavigationView = (activity as MainActivity).bottomNavigationView

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val currentDate = Calendar.getInstance().timeInMillis

        timer = object : Runnable {
            override fun run() {
                timeInMilliseconds = SystemClock.uptimeMillis() - startTime
                updatedTime = timeSwapBuff + timeInMilliseconds

                val secs = (updatedTime / 1000).toInt()
                val mins = secs / 60
                val hrs = mins / 60
                val milliseconds = (updatedTime % 1000 / 10).toInt()

                timerTextView?.text = getString(R.string.timer_format, hrs, mins % 60, secs % 60, milliseconds)
                handler.postDelayed(this, 50)
            }
        }

        startButton.setOnClickListener {
            startTracking()
            for (i in 0 until bottomNavigationView.menu.size()) {
                bottomNavigationView.menu.getItem(i).isEnabled = false
            }
            (activity as MainActivity).modeSwitch.isEnabled = false
            startTime = SystemClock.uptimeMillis()
            handler.postDelayed(timer, 0)
            startButton.visibility = View.GONE
            finishButton.visibility = View.VISIBLE
        }


        finishButton.setOnClickListener {
            val timeParts = timerTextView?.text.toString().split(":")
            val hours = timeParts[0].toLong()
            val minutes = timeParts[1].toLong()
            val seconds = timeParts[2].toLong()
            val centiSeconds = timeParts[3].toLong()

            val totalTime = hours * 60 * 60 * 100 + minutes * 60 * 100 + seconds * 100 + centiSeconds

            startTime = 0L
            timeInMilliseconds = 0L
            timeSwapBuff = 0L
            updatedTime = 0L
            timerTextView?.text = getString(R.string.timer)
            handler.removeCallbacks(timer)
            startButton.visibility = View.VISIBLE
            finishButton.visibility = View.GONE

            stopTracking()
            insertIntoDB(currentDate, "Run", totalTime)

            for (i in 0 until bottomNavigationView.menu.size()) {
                bottomNavigationView.menu.getItem(i).isEnabled = true
            }
            (activity as MainActivity).modeSwitch.isEnabled = true
        }
    }
}