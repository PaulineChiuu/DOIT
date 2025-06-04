package com.example.doit2.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.doit2.TaskGoal
import com.example.doit2.databinding.ItemTaskGoalBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TaskGoalAdapter(
    private val onToggleComplete: (TaskGoal) -> Unit,
    private val onEditClick: (TaskGoal) -> Unit,
    private val onDeleteClick: (TaskGoal) -> Unit,
    private val onProgressUpdate: (TaskGoal, Int) -> Unit
) : ListAdapter<TaskGoal, TaskGoalAdapter.TaskGoalViewHolder>(TaskGoalDiffCallback()) {

    private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskGoalViewHolder {
        val binding = ItemTaskGoalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskGoalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskGoalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskGoalViewHolder(
        private val binding: ItemTaskGoalBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(goal: TaskGoal) {
            binding.apply {
                // 基本資訊
                tvTitle.text = goal.title
                tvDescription.text = goal.description

                // 分類和優先級
                tvCategory.text = "${goal.category.emoji} ${goal.category.displayName}"
                tvPriority.text = goal.priority.displayName

                // 分類顏色
                try {
                    cardCategory.setCardBackgroundColor(Color.parseColor(goal.category.color))
                } catch (e: Exception) {
                    // 使用預設顏色
                }

                // 優先級顏色
                try {
                    tvPriority.setTextColor(Color.parseColor(goal.priority.color))
                } catch (e: Exception) {
                    // 使用預設顏色
                }

                // 目標日期和剩餘天數
                tvTargetDate.text = "目標：${dateFormat.format(goal.targetDate)}"
                val daysLeft = calculateDaysLeft(goal.targetDate)
                tvDaysLeft.text = when {
                    daysLeft > 0 -> "還有 $daysLeft 天"
                    daysLeft == 0 -> "今天到期"
                    else -> "已逾期 ${-daysLeft} 天"
                }

                // 設定逾期顏色
                if (daysLeft < 0) {
                    tvDaysLeft.setTextColor(Color.parseColor("#F44336"))
                } else if (daysLeft <= 3) {
                    tvDaysLeft.setTextColor(Color.parseColor("#FF9800"))
                } else {
                    tvDaysLeft.setTextColor(Color.parseColor("#4CAF50"))
                }

                // 進度
                progressBar.progress = goal.progress
                tvProgress.text = "${goal.progress}%"

                // 進度調整
                seekProgress.progress = goal.progress
                seekProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            tvProgress.text = "$progress%"
                            progressBar.progress = progress
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        seekBar?.let {
                            onProgressUpdate(goal, it.progress)
                        }
                    }
                })

                // 完成狀態
                checkCompleted.isChecked = goal.isCompleted
                checkCompleted.setOnCheckedChangeListener { _, isChecked ->
                    onToggleComplete(goal)
                }

                // 卡片樣式（已完成的目標顯示不同樣式）
                if (goal.isCompleted) {
                    root.alpha = 0.7f
                    tvTitle.paintFlags = tvTitle.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    root.alpha = 1.0f
                    tvTitle.paintFlags = tvTitle.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }

                // 點擊事件
                btnEdit.setOnClickListener {
                    onEditClick(goal)
                }

                btnDelete.setOnClickListener {
                    onDeleteClick(goal)
                }

                // 長按編輯
                root.setOnLongClickListener {
                    onEditClick(goal)
                    true
                }
            }
        }

        private fun calculateDaysLeft(targetDate: Date): Int {
            val now = Date()
            val diffInMillis = targetDate.time - now.time
            return TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()
        }
    }
}

class TaskGoalDiffCallback : DiffUtil.ItemCallback<TaskGoal>() {
    override fun areItemsTheSame(oldItem: TaskGoal, newItem: TaskGoal): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TaskGoal, newItem: TaskGoal): Boolean {
        return oldItem == newItem
    }
}