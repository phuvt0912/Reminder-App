package com.example.reminder

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import com.google.gson.Gson

object ReminderHelper {
    val gson = Gson()
    fun createNotificationChannel(context: Context)
    {
        val audioAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)   // hoặc USAGE_ALARM nếu muốn bypass DND
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val soundurl = Uri.parse("android.resource://${context.packageName}/${R.raw.soundeffect}")
        val channel_id = "Task Notification"
        val channel_name = "Task Notification"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(
            channel_id,
            channel_name,
            importance
        ).apply {
            setSound(soundurl, audioAttrs)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
    fun convertDate(date: String): LocalDate {
        val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
        return LocalDate.parse(date, formatter)
    }
    fun convertTime(time: String): LocalTime {
        val formatter = DateTimeFormatter.ofPattern("H:mm")
        return LocalTime.parse(time, formatter)
    }
    fun sortReminders(reminders: MutableList<ReminderItem>) {
        reminders.sortWith(compareBy(
            { convertDate(it.date) },
            { convertTime(it.time) }
        ))
    }

    fun loadData(context: Context, reminders: MutableList<ReminderItem>) {
        val sharedPref = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        val json = sharedPref.getString("reminder_list", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<ReminderItem>>() {}.type
            val loadedList: MutableList<ReminderItem> = gson.fromJson(json, type)
            reminders.addAll(loadedList)
            for (reminder in reminders)
            {
                scheduleTask(context,reminder, context.getSystemService(ALARM_SERVICE) as AlarmManager)
            }
        }
        Toast.makeText(context, "Có ${reminders.size} lịch đã đặt trước", Toast.LENGTH_SHORT).show()
    }

    fun saveData(context: Context, reminders: MutableList<ReminderItem>) {
        val sharedPref = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        val json = gson.toJson(reminders)
        editor.putString("reminder_list", json)
        editor.apply()
    }

    fun scheduleTask(context: Context, task: ReminderItem, alarm: AlarmManager)
    {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("title", "Nhắc nhở lịch")
            putExtra("task", task.task)
            putExtra("date", task.date)
            putExtra("time", task.time)
            putExtra("id", task.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
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
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, taskmils.timeInMillis, pendingIntent)
    }
    fun removeSchedule(context: Context,task:ReminderItem, alarm: AlarmManager) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("title", "Nhắc nhở lịch")
            putExtra("task", task.task)
            putExtra("date", task.date)
            putExtra("time", task.time)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarm.cancel(pendingIntent)
    }

    fun openReminderDialog(context: Context,
                                   existingItem: ReminderItem?,
                                   alarm: AlarmManager,
                                   reminders: MutableList<ReminderItem>,
                                   adapter: ReminderAdapter) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.itemreminder_dialog, null)

        val timePicker: TimePicker = dialogView.findViewById(R.id.timePicker)
        val datepicker: ImageButton = dialogView.findViewById(R.id.btnSelectDate)
        val selectedDateText: TextView = dialogView.findViewById(R.id.selectedDateText)
        val task: EditText = dialogView.findViewById(R.id.editTask)
        val saveButton: Button = dialogView.findViewById(R.id.btnSave)
        val cancelButton: Button = dialogView.findViewById(R.id.btnCancel)

        timePicker.setIs24HourView(true)

        // Biến lưu ngày tháng năm
        var selectedYear: Int? = null
        var selectedMonth: Int? = null
        var selectedDay: Int? = null
        lateinit var dateText: String

        if (existingItem != null) {
            task.setText(existingItem.task)

            // Thời gian
            timePicker.hour = existingItem.time.split(":")[0].toInt()
            timePicker.minute = existingItem.time.split(":")[1].toInt()

            // Ngày tháng năm
            selectedDay = existingItem.date.split("/")[0].toInt()
            selectedMonth = existingItem.date.split("/")[1].toInt() - 1
            selectedYear = existingItem.date.split("/")[2].toInt()
            dateText = existingItem.date
            selectedDateText.text = existingItem.date
        }

        // Xử lý chọn ngày
        datepicker.setOnClickListener {
            val calendar = Calendar.getInstance()

            val year = selectedYear ?: calendar.get(Calendar.YEAR)
            val month = selectedMonth ?: calendar.get(Calendar.MONTH)
            val day = selectedDay ?: calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                context,
                { _, y, m, d ->
                    selectedYear = y
                    selectedMonth = m
                    selectedDay = d
                    dateText = "$d/${m + 1}/$y"
                    selectedDateText.text = dateText
                },
                year, month, day
            )

            datePicker.show()
        }

        // Tạo dialog
        val dialog = AlertDialog.Builder(context)
            .setTitle(if (existingItem == null) "Thêm lịch" else "Sửa lịch")
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            val taskText = task.text.toString()
            val timestr = String.format("%02d:%02d", hour, minute)

            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
            val formattedDate = today.format(formatter)

            if (taskText.isEmpty() || selectedYear == null || selectedMonth == null || selectedDay == null) {
                Toast.makeText(context, "Vui lòng nhập đầy đủ nội dung", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (convertTime(timestr) < LocalTime.now())
            {
                Toast.makeText(context, "Thời gian không hợp lệ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (convertDate(dateText) < convertDate(formattedDate))
            {
                Toast.makeText(context, "Ngày không hợp lệ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (existingItem == null) {
                // chế độ thêm lịch
                val newId =
                    if (reminders.isEmpty()) 0 else reminders.maxOf { it.id } + 1

                val newItem = ReminderItem(newId, timestr, dateText, taskText)

                reminders.add(newItem)
                sortReminders(reminders)
                val position = reminders.indexOf(newItem)
                adapter.notifyItemInserted(position)
                saveData(context, reminders)

                scheduleTask(context,newItem, alarm)
                Toast.makeText(context, "Thêm thành công, ${newItem.id}", Toast.LENGTH_SHORT).show()

            } else {
                // EDIT MODE
                existingItem.time = timestr
                existingItem.date = dateText
                existingItem.task = taskText

                sortReminders(reminders)
                val position = reminders.indexOf(existingItem)
                adapter.notifyItemInserted(position)
                saveData(context, reminders)

                removeSchedule(context,existingItem, alarm)
                scheduleTask(context,existingItem, alarm)

                Toast.makeText(context, "Sửa thành công, ${existingItem.id}", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }

        cancelButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}