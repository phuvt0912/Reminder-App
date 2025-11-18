package com.example.reminder

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import android.widget.Toast
class MainActivity : AppCompatActivity() {
    private lateinit var adapter: ReminderAdapter
    private val reminders = mutableListOf<ReminderItem>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        //Tạo 1 kênh thông báo
        ReminderHelper.createNotificationChannel(this)

        //Tạo alarm manager
        var alarm = getSystemService(ALARM_SERVICE) as AlarmManager

        val reminderView: RecyclerView = findViewById<RecyclerView>(R.id.ReminderSchedule)
        val addButton: Button = findViewById<Button>(R.id.btnAdd)
        ReminderHelper.loadData(this, reminders)
        ReminderHelper.sortReminders(reminders)

            //Khai báo adapter có 3 tham số truyên vào
        // 1. Danh sách lịch
        // 2. Hàm callback xóa lịch
        // 3.. Hàm callback sửa thông tin lịch
        adapter = ReminderAdapter(
            reminders,
            onDeleteClick = { item ->
                reminders.remove(item)
                val position = reminders.indexOf(item)
                adapter.notifyItemRemoved(position)
                ReminderHelper.saveData(this, reminders) //
                ReminderHelper.removeSchedule(this,item, alarm)
                Toast.makeText(this, "Xóa thành công, ${item.id}", Toast.LENGTH_SHORT).show()
            },
            //Sự kiện click vào item để chỉnh sửa
            onItemClick = {item, ->
                ReminderHelper.openReminderDialog(this,
                    item,
                    alarm,
                    reminders,
                    adapter)
            }
        )
        // Kết thúc khai báo adapter
        //Gán gán adapter vừa khai báo vào adapter của recycleView và hiển thị ở màn hình hiện tại (this context)
        reminderView.adapter = adapter // Hiểu là thông qua adapter, trông RecycleView sẽ như thế nào
        reminderView.layoutManager = LinearLayoutManager(this) // Hiểu là gắn cái layout đó ở ngữ cảnh nào (this là ở ngữ cảnh này)

        //Xử lý sự kiện của nút thêm lịch
        addButton.setOnClickListener {
            ReminderHelper.openReminderDialog(
                this,
                null,
                alarm,
                reminders,
                adapter
            )
        }
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

    }