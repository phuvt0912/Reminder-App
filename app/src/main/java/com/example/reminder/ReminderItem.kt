package com.example.reminder

class ReminderItem(
    val time: String,
    val date: String,
    val task: String
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