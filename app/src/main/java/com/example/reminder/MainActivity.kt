package com.example.reminder

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TimePicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

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
        val reminderView: RecyclerView = findViewById<RecyclerView>(R.id.ReminderSchedule)
        val addButton: Button = findViewById<Button>(R.id.btnAdd)
        loadData()
        adapter = ReminderAdapter(
            reminders,
            onSwitchChange = { item, isChecked ->
                item.isActive = isChecked
                saveData() //
            },
            onDeleteClick = { item ->
                reminders.remove(item)
                adapter.notifyDataSetChanged()
                saveData() //
            }
        )
        reminderView.adapter = adapter
        reminderView.layoutManager = LinearLayoutManager(this)
        addButton.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.itemreminder_dialog, null)
            val timePicker: TimePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
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
                reminders.add(ReminderItem(timestr, taskText, true)) //hêm vào list
                adapter.notifyItemInserted(reminders.size - 1)//báo adapter là có thay đổi
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