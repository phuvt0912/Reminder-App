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
    private val onSwitchChange: (ReminderItem, Boolean) -> Unit,
    private val onDeleteClick: (ReminderItem) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.TimeText)
        val tvTask: TextView = itemView.findViewById(R.id.TaskText)
        val switchActive: Switch = itemView.findViewById(R.id.switchActive)
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
        holder.tvTask.text = reminder.task
        holder.switchActive.isChecked = reminder.isActive

        // Khi bật/tắt switch
        holder.switchActive.setOnCheckedChangeListener { _, isChecked ->
            onSwitchChange(reminder, isChecked)
        }

        // Khi nhấn nút xóa
        holder.btnDelete.setOnClickListener {
            onDeleteClick(reminder)
        }
    }

    override fun getItemCount() = reminders.size
}
