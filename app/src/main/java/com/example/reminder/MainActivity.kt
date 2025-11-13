package com.example.reminder

import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: ReminderAdapter
    private val reminders = mutableListOf<ReminderItem>()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val reminderView: RecyclerView = findViewById<RecyclerView>(R.id.ReminderSchedule)
        val addButton: Button = findViewById<Button>(R.id.btnAdd)

        addButton.setOnClickListener {
        }
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadData()
    {
        val sharedPref = getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        val json = sharedPref.getString("reminder_list", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<ReminderItem>>() {}.type
            val loadedList: MutableList<ReminderItem> = gson.fromJson(json, type)
            reminders.addAll(loadedList)
        }
    }

    private fun saveData()
    {
        val sharedPref = getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        val json = gson.toJson(reminders)
        editor.putString("reminder_list", json)
        editor.apply()
    }
}