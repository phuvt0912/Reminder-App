package com.example.reminder

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.Image
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
import java.time.Month
import java.time.MonthDay
import java.time.Year

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

    private fun scheduleTask(task: ReminderItem, alarm: AlarmManager)
    {
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("title", "Nhắc nhở lịch")
            putExtra("task", task.task)
            putExtra("date", task.date)
            putExtra("time", task.time)
            putExtra("id", task.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            task.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        var taskmils = Calendar.getInstance()
        taskmils.set(Calendar.YEAR, task.date.split("/")[2].toInt())
        taskmils.set(Calendar.MONTH, task.date.split("/")[1].toInt() - 1)
        taskmils.set(Calendar.DAY_OF_MONTH, task.date.split("/")[0].toInt())
        taskmils.set(Calendar.HOUR_OF_DAY, task.time.split(":")[0].toInt())
        taskmils.set(Calendar.MINUTE, task.time.split(":")[1].toInt())
        alarm.setExact(AlarmManager.RTC_WAKEUP, taskmils.timeInMillis, pendingIntent)
    }
    private fun removeSchedule(task: ReminderItem, alarm: AlarmManager) {
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("title", "Nhắc nhở lịch")
            putExtra("task", task.task)
            putExtra("date", task.date)
            putExtra("time", task.time)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            task.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarm.cancel(pendingIntent)
    }
    private lateinit var adapter: ReminderAdapter
    private val reminders = mutableListOf<ReminderItem>()
    private val gson = Gson()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        //Tạo 1 kênh thông báo
        createNotificationChannel()

        //Tạo alarm manager
        var alarm = getSystemService(ALARM_SERVICE) as AlarmManager

        val reminderView: RecyclerView = findViewById<RecyclerView>(R.id.ReminderSchedule)
        val addButton: Button = findViewById<Button>(R.id.btnAdd)
        loadData()
        sortReminders()

            //Khai báo adapter có 3 tham số truyên vào
        // 1. Danh sách lịch
        // 2. Hàm callback xóa lịch
        // 3.. Hàm callback sửa thông tin lịch
        adapter = ReminderAdapter(
            reminders,
            onDeleteClick = { item ->
                reminders.remove(item)
                adapter.notifyDataSetChanged()
                saveData() //
                removeSchedule(item, alarm)
                Toast.makeText(this, "Xóa thành công, ${item.id}", Toast.LENGTH_SHORT).show()
            },
            onItemClick = {item, ->
                //tạo dialogview để tý nữa popup lên
                val dialogView = LayoutInflater.from(this).inflate(R.layout.itemreminder_dialog, null)
                val timePicker: TimePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
                val datepicker: ImageButton = dialogView.findViewById<ImageButton>(R.id.btnSelectDate)
                var selectedDateText: TextView = dialogView.findViewById<TextView>(R.id.selectedDateText)
                lateinit var dateText: String

                var selectedYear: Int? = null
                var selectedMonth: Int? = null
                var selectedDay: Int? = null
                // Xử lý sự kiện khi nút chọn ngày được nhấn
                datepicker.setOnClickListener {
                    //tạo 1 thể hiện của lớp Calendar
                    val calendar = Calendar.getInstance()
                    // Ánh xạ text và lưu các biến ngày tháng năm

                    val year = item.date.split("/")[2].toInt() //lấy năm của task hiện tại
                    val month = item.date.split("/")[1].toInt() - 1 // Lấy tháng của task hiện tại
                    val day = item.date.split("/")[0].toInt() // Lấy ngày của task hiện tại



                    // Khởi tạo hộp thoại xổ ra để chọn ngày tháng năm
                    val datePicker = DatePickerDialog(
                        this, //ngữ cảnh hiển thị hộp thoại
                        { _, selectedYear, selectedMonth, selectedDay -> //hàm callback
                            val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                            selectedDateText.text = date
                            dateText = date
                        },
                        //Gía trị hiển thị mặc định
                        year, month, day
                    )
                    datePicker.show()
                }
                timePicker.setIs24HourView(true)
                var task: EditText = dialogView.findViewById<EditText>(R.id.editTask)
                val saveButton: Button = dialogView.findViewById<Button>(R.id.btnSave)
                val cancelButton: Button = dialogView.findViewById<Button>(R.id.btnCancel)

                //Hiển thị lại thông tin cũ và gán lại giá trị của lịch cũ
                task.setText(item.task)
                timePicker.hour = item.time.split(":")[0].toInt()
                timePicker.minute = item.time.split(":")[1].toInt()

                selectedDay = item.date.split("/")[0].toInt()
                selectedMonth = item.date.split("/")[1].toInt() - 1
                selectedYear = item.date.split("/")[2].toInt()

                selectedDateText.text = item.date
                dateText = item.date

                //Tạo dialog để chứa dialogview
                val dialog = AlertDialog.Builder(this)
                    .setTitle("Sửa lịch")
                    .setView(dialogView)
                    .create()

                saveButton.setOnClickListener {
                    val hour = timePicker.hour
                    val minute = timePicker.minute
                    val taskText = task.text.toString()
                    val timestr = String.format("%02d:%02d", hour, minute)
                    if (taskText.isEmpty()  || timestr.isEmpty() || (selectedDay == null) || selectedMonth == null || selectedYear == null)
                    {
                        Toast.makeText(this, "Vui lòng nhập đầy đủ nội dung", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    item.time = timestr
                    item.date = dateText
                    item.task = taskText
                    sortReminders()
                    adapter.notifyDataSetChanged()
                    saveData() //lưu vào sharedprefs
                    dialog.dismiss() //tắt hộp thoại
                    removeSchedule(item, alarm)
                    scheduleTask(item, alarm, )
                    Toast.makeText(this, "Sửa thành công, ${item.id}", Toast.LENGTH_SHORT).show()
                }
                cancelButton.setOnClickListener { dialog.dismiss() }
                dialog.show()

            }
        )
        // Kết thúc khai báo adapter
        //Gán gán adapter vừa khai báo vào adapter của recycleView và hiển thị ở màn hình hiện tại (this context)
        reminderView.adapter = adapter // Hiểu là thông qua adapter, trông RecycleView sẽ như thế nào
        reminderView.layoutManager = LinearLayoutManager(this) // Hiểu là gắn cái layout đó ở ngữ cảnh nào (this là ở ngữ cảnh này)

        //Xử lý sự kiện của nút thêm lịch
        addButton.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.itemreminder_dialog, null)
            val timePicker: TimePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
            val datepicker: ImageButton = dialogView.findViewById<ImageButton>(R.id.btnSelectDate)
            lateinit var dateText: String
            var selectedYear: Int? = null
            var selectedMonth: Int? = null
            var selectedDay: Int? = null

            datepicker.setOnClickListener {
                val calendar = Calendar.getInstance()
                val selectedDateText: TextView = dialogView.findViewById<TextView>(R.id.selectedDateText)
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)



                val datePicker = DatePickerDialog(
                    this,
                    { _, sltYear, sltMonth, sltDay ->
                        val date = "$sltDay/${sltMonth + 1}/$sltYear"
                        selectedDay = sltDay
                        selectedMonth = sltMonth
                        selectedYear = sltYear
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
                if (taskText.isEmpty()  || timestr.isEmpty() || (selectedDay == null) || selectedMonth == null || selectedYear == null)
                {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ nội dung", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                reminders.add(ReminderItem(timestr, dateText, taskText))
                sortReminders()
                adapter.notifyDataSetChanged()
                saveData() //lưu vào sharedprefs
                dialog.dismiss() //tắt hộp thoại
                scheduleTask(reminders.last(), alarm)
                Toast.makeText(this, "Thêm thành công, ${reminders.last().id}", Toast.LENGTH_SHORT).show()
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