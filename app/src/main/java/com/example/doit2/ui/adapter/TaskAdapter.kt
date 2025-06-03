package com.example.doit2.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.doit2.R
import com.example.doit2.data.model.Task
import com.example.doit2.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onTaskToggle: (Task) -> Unit,
    private val onTaskEdit: (Task) -> Unit,
    private val onTaskDelete: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task)
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.apply {
                // 設定任務資訊
                tvTaskTitle.text = task.title
                tvTaskDescription.text = task.description
                cbTaskComplete.isChecked = task.isCompleted

                // 設定創建時間
                tvCreatedTime.text = formatDate(task.createdAt)

                // 設定描述可見性
                if (task.description.isEmpty()) {
                    tvTaskDescription.visibility = View.GONE
                } else {
                    tvTaskDescription.visibility = View.VISIBLE
                }

                // 設定完成狀態樣式
                updateCompletionStyle(task.isCompleted)

                // 點擊事件
                root.setOnClickListener {
                    onTaskClick(task)
                }

                // CheckBox 點擊事件
                cbTaskComplete.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked != task.isCompleted) {
                        onTaskToggle(task.copy(isCompleted = isChecked))
                        updateCompletionStyle(isChecked)
                    }
                }

                // 選項菜單點擊事件
                ivTaskMenu.setOnClickListener {
                    showPopupMenu(it, task)
                }
            }
        }

        private fun updateCompletionStyle(isCompleted: Boolean) {
            binding.apply {
                if (isCompleted) {
                    // 已完成樣式
                    tvTaskTitle.alpha = 0.6f
                    tvTaskDescription.alpha = 0.6f
                    tvCreatedTime.alpha = 0.6f
                    viewCompletedOverlay.visibility = View.VISIBLE
                    viewCompletedOverlay.alpha = 0.1f
                } else {
                    // 未完成樣式
                    tvTaskTitle.alpha = 1.0f
                    tvTaskDescription.alpha = 1.0f
                    tvCreatedTime.alpha = 1.0f
                    viewCompletedOverlay.visibility = View.GONE
                }
            }
        }

        private fun showPopupMenu(view: View, task: Task) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.task_menu, popup.menu)

            // 動態設定菜單項目文字
            val toggleItem = popup.menu.findItem(R.id.action_toggle_completion)
            toggleItem?.title = if (task.isCompleted) {
                view.context.getString(R.string.task_menu_mark_pending)
            } else {
                view.context.getString(R.string.task_menu_mark_completed)
            }

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit_task -> {
                        onTaskEdit(task)
                        true
                    }
                    R.id.action_toggle_completion -> {
                        onTaskToggle(task.copy(isCompleted = !task.isCompleted))
                        true
                    }
                    R.id.action_delete_task -> {
                        onTaskDelete(task)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        private fun formatDate(date: Date): String {
            val now = Date()
            val calendar = Calendar.getInstance()
            val todayCalendar = Calendar.getInstance()

            calendar.time = date
            todayCalendar.time = now

            return when {
                // 今天
                calendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                        calendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR) -> {
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    "今天 ${timeFormat.format(date)}"
                }
                // 昨天
                calendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                        calendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR) - 1 -> {
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    "昨天 ${timeFormat.format(date)}"
                }
                // 其他
                else -> {
                    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
                    dateFormat.format(date)
                }
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}