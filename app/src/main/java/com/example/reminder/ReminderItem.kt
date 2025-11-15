package com.example.reminder

class ReminderItem(
    var time: String,
    var date: String,
    var task: String
)
{
    val id: Int = getNextid()
    companion object {
        private var nextId: Int = 0
        fun getNextid(): Int {
            return nextId++
        }

    }
}