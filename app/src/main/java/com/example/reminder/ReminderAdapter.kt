package com.example.reminder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReminderAdapter(
    private val reminders: MutableList<ReminderItem>,
    private val onDeleteClick: (ReminderItem) -> Unit,
    private val onItemClick: (ReminderItem) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.TimeText)
        val tvDate: TextView = itemView.findViewById(R.id.DateText)
        val tvTask: TextView = itemView.findViewById(R.id.TaskText)
        val btnDelete: ImageButton = itemView.findViewById(R.id.DeleteTaskButton)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]
        holder.tvTime.text = reminder.time
        holder.tvDate.text = reminder.date
        holder.tvTask.text = reminder.task

        // Khi nhấn nút xóa
        holder.btnDelete.setOnClickListener {
            onDeleteClick(reminder)
        }

        holder.itemView.setOnClickListener {
            onItemClick(reminder)
        }
    }


    override fun getItemCount() = reminders.size
}
