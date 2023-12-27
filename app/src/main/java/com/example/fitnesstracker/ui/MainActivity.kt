package com.example.fitnesstracker.ui

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import android.Manifest
import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.fitnesstracker.R
import com.example.fitnesstracker.ui.fragments.HistoryFragment
import com.example.fitnesstracker.ui.fragments.ScheduleFragment
import com.example.fitnesstracker.ui.fragments.RunningFragment
import com.example.fitnesstracker.ui.fragments.StatisticsFragment
import com.example.fitnesstracker.ui.fragments.WalkingFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val permissionRequests = 1

    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var modeSwitch: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check for the necessary permissions.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Permissions are not granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION, Manifest.permission.ACCESS_FINE_LOCATION),
                    permissionRequests)
            }
        }

        // Add the WalkingFragment by default.
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment, WalkingFragment())
            .commit()

        modeSwitch = findViewById(R.id.switch1)
        val imgExercise = findViewById<ImageView>(R.id.exImg)
        val title = findViewById<TextView>(R.id.title)

        modeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                imgExercise.setImageResource(R.drawable.ic_run)
                // Replace the current fragment with the RunningFragment.
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, RunningFragment())
                    .commit()
            } else {
                imgExercise.setImageResource(R.drawable.ic_walk)
                // Replace the current fragment with the WalkingFragment.
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, WalkingFragment())
                    .commit()
            }
        }

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.navigation_exercise
        bottomNavigationView.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.navigation_schedule -> {
                    modeSwitch.visibility = View.GONE
                    imgExercise.visibility = View.GONE
                    title.text = getString(R.string.schedule_exercise)
                    ScheduleFragment()
                }
                R.id.navigation_exercise -> {
                    modeSwitch.visibility = View.VISIBLE
                    imgExercise.visibility = View.VISIBLE
                    title.text = getString(R.string.activity)
                    if (modeSwitch.isChecked) RunningFragment() else WalkingFragment()
                }
                R.id.navigation_history -> {
                    modeSwitch.visibility = View.GONE
                    imgExercise.visibility = View.GONE
                    title.text = getString(R.string.past_exercises)
                    HistoryFragment()
                }
                R.id.navigation_statistics -> {
                    modeSwitch.visibility = View.GONE
                    imgExercise.visibility = View.GONE
                    title.text = getString(R.string.statistics)
                    StatisticsFragment()
                }
                else -> null
            }

            if (fragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, fragment)
                    .commit()
            }
            true
        }
    }
}
