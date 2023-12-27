package com.example.fitnesstracker.ui

import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.fitnesstracker.R
import com.example.fitnesstracker.db.AppDatabase
import com.example.fitnesstracker.db.ScheduleExercise
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class ScheduleActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        var schTime = 0L
        var schDate = 0L
        var hour: Int
        var minute: Int

        var exerciseMode = ""
        var reminder: Boolean

        val timeImg: ImageView = findViewById(R.id.timeImg)
        val dateImg: ImageView = findViewById(R.id.scheduleImg)
        val timeTextView: TextView = findViewById(R.id.time)
        val tvDate: TextView = findViewById(R.id.date)
        val swExercise: SwitchMaterial = findViewById(R.id.schSwitch)
        val cbReminder: CheckBox = findViewById(R.id.reminder)
        val btnSave = findViewById<Button>(R.id.save_btn)

        timeImg.setOnClickListener {
            val picker =
                MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(0)
                    .setTitleText("Select Appointment time")
                    .build()

            picker.addOnPositiveButtonClickListener {
                // Get the selected hour and minute.
                hour = if (picker.hour > 12) picker.hour - 12 else picker.hour
                minute = picker.minute
                val timePeriod = if (picker.hour < 12) "AM" else "PM"
                val formattedTime = String.format("%02d:%02d %s", hour, minute, timePeriod)

                // Create a Calendar object set to the selected date.
                val timeCalendar = Calendar.getInstance()
                timeCalendar.timeInMillis = schDate
                // Set the hour and minute of the selected date to the selected time.
                timeCalendar.set(Calendar.HOUR_OF_DAY, picker.hour)
                timeCalendar.set(Calendar.MINUTE, picker.minute)

                // Update schTime with the selected date and time.
                schTime = timeCalendar.timeInMillis

                // Display the formatted time in the date TextView.
                timeTextView.text = formattedTime
            }
            picker.show(supportFragmentManager, "tag")
        }


        dateImg.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Display the selected date in the date TextView.
                    val selectedDate = String.format(
                        "%02d/%02d/%04d",
                        selectedDay,
                        selectedMonth + 1,
                        selectedYear
                    )

                    // Create a Calendar object set to the selected date.
                    val dateCalendar = Calendar.getInstance()
                    dateCalendar.set(Calendar.YEAR, selectedYear)
                    dateCalendar.set(Calendar.MONTH, selectedMonth)
                    dateCalendar.set(Calendar.DAY_OF_MONTH, selectedDay)

                    // Get the Unix timestamp.
                    schDate = dateCalendar.timeInMillis
                    tvDate.text = selectedDate
                },
                year,
                month,
                day
            )

            datePickerDialog.show()
        }

        swExercise.setOnCheckedChangeListener { _, isChecked ->
            exerciseMode = if (isChecked) {
                "Run"
            } else {
                "Walk"
            }
        }

        val schExercise = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("schExercise", ScheduleExercise::class.java)
        } else {
            intent.getParcelableExtra("schExercise")
        }

        if (schExercise != null) {
            // If there is passed data, populate the layout elements with this data.
            val date = Date(schExercise.date)
            val time = Date(schExercise.time)

            // Create SimpleDateFormat objects and format the Date objects to strings.
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val dateString = dateFormat.format(date)
            val timeString = timeFormat.format(time)

            tvDate.text = dateString
            timeTextView.text = timeString
            swExercise.isChecked = schExercise.type == "Run"
            cbReminder.isChecked = schExercise.reminder

            schDate = schExercise.date
            schTime = schExercise.time
        }

        btnSave.setOnClickListener {
            // Create a Calendar object for the current date and time.
            val nowCalendar = Calendar.getInstance()

            // Create a Calendar object for the selected date.
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.timeInMillis = schDate

            // Set the hour and minute of selectedCalendar to the selected time.
            val timeCalendar = Calendar.getInstance()
            timeCalendar.timeInMillis = schTime
            selectedCalendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
            selectedCalendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))

            // Check if the selected date and time are in the future.

            if (selectedCalendar.before(nowCalendar) || schTime == 0L) {
            // Show a SnackBar with an error message.
            Snackbar.make(btnSave, "The selected date and time are invalid.", Snackbar.LENGTH_SHORT).show()
            } else {
                reminder = cbReminder.isChecked
                // The selected date and time are valid. Save the data.
                saveData(schExercise, schDate, schTime, exerciseMode, reminder)
            }
        }
    }

    private fun saveData(schExercise: ScheduleExercise?, schDate: Long, schTime: Long, exerciseMode: String, reminder: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = Room.databaseBuilder(
                this@ScheduleActivity,
                AppDatabase::class.java, AppDatabase.ScheduleExerciseDB
            ).build()

            val scheduledExercise =
                ScheduleExercise(schExercise?.id, schDate, schTime, exerciseMode, reminder)
            if (schExercise != null) {
                // If there is passed data, update the existing item.
                db.scheduledExerciseDao().update(scheduledExercise)
            } else {
                // If there isn't, insert a new item.
                db.scheduledExerciseDao().insert(scheduledExercise)
            }
            finish()
        }
    }
}