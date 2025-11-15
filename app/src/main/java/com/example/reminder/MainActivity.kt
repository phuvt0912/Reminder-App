package com.example.reminder

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import java.util.Calendar
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import android.os.Build
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {

    private fun createNotificationChannel()
    {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
        {
            val channel_id = "Task Notification"
            val channel_name = "Task Notification"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                channel_id,
                channel_name,
                importance
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    private fun convertDate(date: String): LocalDate {
        val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
        return LocalDate.parse(date, formatter)
    }
    private fun convertTime(time: String): LocalTime {
        val formatter = DateTimeFormatter.ofPattern("H:mm")
        return LocalTime.parse(time, formatter)
    }
    private fun sortReminders() {
        reminders.sortWith(compareBy(
            { convertDate(it.date) },
            { convertTime(it.time) }
        ))
    }

    private fun loadData() {
        val sharedPref = getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        val json = sharedPref.getString("reminder_list", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<ReminderItem>>() {}.type
            val loadedList: MutableList<ReminderItem> = gson.fromJson(json, type)
            reminders.addAll(loadedList)
        }
    }

    private fun saveData() {
        val sharedPref = getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        val json = gson.toJson(reminders)
        editor.putString("reminder_list", json)
        editor.apply()
    }


    private lateinit var adapter: ReminderAdapter
    private val reminders = mutableListOf<ReminderItem>()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        createNotificationChannel()
        val reminderView: RecyclerView = findViewById<RecyclerView>(R.id.ReminderSchedule)
        val addButton: Button = findViewById<Button>(R.id.btnAdd)
        loadData()
        sortReminders()
        adapter = ReminderAdapter(
            reminders,
            onDeleteClick = { item ->
                reminders.remove(item)
                adapter.notifyDataSetChanged()
                saveData() //
            }
        )
        reminderView.adapter = adapter // Hiểu là thông qua adapter, trông RecycleView sẽ như thế nào
        reminderView.layoutManager = LinearLayoutManager(this) // Hiểu là gắn cái layout đó ở ngữ cảnh nào (this là ở ngữ cảnh này)
        addButton.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.itemreminder_dialog, null)
            val timePicker: TimePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
            val datepicker: ImageButton = dialogView.findViewById<ImageButton>(R.id.btnSelectDate)
            lateinit var dateText: String

            datepicker.setOnClickListener {
                val calendar = Calendar.getInstance()
                val selectedDateText: TextView = dialogView.findViewById<TextView>(R.id.selectedDateText)
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val datePicker = DatePickerDialog(
                    this,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                        selectedDateText.text = date
                        dateText = date
                    },
                    year, month, day
                )

                datePicker.show()
            }
            timePicker.setIs24HourView(true)
            val task: EditText = dialogView.findViewById<EditText>(R.id.editTask)
            val saveButton: Button = dialogView.findViewById<Button>(R.id.btnSave)
            val cancelButton: Button = dialogView.findViewById<Button>(R.id.btnCancel)

            val dialog = AlertDialog.Builder(this)
                .setTitle("Thêm lịch")
                .setView(dialogView)
                .create()

            saveButton.setOnClickListener {
                val hour = timePicker.hour
                val minute = timePicker.minute
                val taskText = task.text.toString()
                val timestr = String.format("%02d:%02d", hour, minute)
                reminders.add(ReminderItem(timestr, dateText, taskText))
                sortReminders()
                adapter.notifyDataSetChanged()
                saveData() //lưu vào sharedprefs
                dialog.dismiss() //tắt hộp thoại
            }
            cancelButton.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

    }