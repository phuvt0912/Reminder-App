package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat

class AlarmReceiver(): BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val title = intent?.getStringExtra("title")
        val task = intent?.getStringExtra("task")
        val date = intent?.getStringExtra("date")
        val time = intent?.getStringExtra("time")
        val id = intent?.getIntExtra("id", 0)



        val message = "Task: $task\nDate: $date\nTime: $time"


        var build = NotificationCompat.Builder(context, "Task Notification")
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .build()

        var notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        notificationManager.notify(id!!, build)

    }
}