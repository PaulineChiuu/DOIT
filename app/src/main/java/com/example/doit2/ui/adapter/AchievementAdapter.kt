package com.example.doit2.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.doit2.R
import com.example.doit2.data.model.Achievement
import com.example.doit2.databinding.ItemAchievementBinding
import java.text.SimpleDateFormat
import java.util.*

class AchievementAdapter(
    private val onAchievementClick: (Achievement) -> Unit = {}
) : ListAdapter<Achievement, AchievementAdapter.AchievementViewHolder>(AchievementDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val binding = ItemAchievementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AchievementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AchievementViewHolder(
        private val binding: ItemAchievementBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(achievement: Achievement) {
            with(binding) {
                // 基本資訊
                tvAchievementTitle.text = achievement.title
                tvAchievementDescription.text = achievement.description
                tvAchievementPoints.text = "+${achievement.points}"
                ivAchievementIcon.setImageResource(achievement.iconRes)

                // 分類顯示
                tvAchievementCategory.text = getCategoryName(achievement.category)

                // 根據解鎖狀態設置外觀
                if (achievement.isUnlocked) {
                    setupUnlockedAppearance(achievement)
                } else {
                    setupLockedAppearance()
                }

                // 點擊事件
                root.setOnClickListener {
                    onAchievementClick(achievement)
                }
            }
        }

        private fun setupUnlockedAppearance(achievement: Achievement) {
            with(binding) {
                // 已解鎖狀態
                tvAchievementStatus.text = "已解鎖"
                tvAchievementStatus.setTextColor(root.context.getColor(R.color.success_color))

                // 顯示解鎖時間
                achievement.unlockedDate?.let { date ->
                    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                    tvUnlockedDate.text = "解鎖於 ${dateFormat.format(date)}"
                    tvUnlockedDate.visibility = View.VISIBLE
                }

                // 正常顏色
                tvAchievementTitle.alpha = 1.0f
                tvAchievementDescription.alpha = 1.0f
                ivAchievementIcon.alpha = 1.0f
                tvAchievementPoints.setTextColor(root.context.getColor(R.color.primary_color))
            }
        }

        private fun setupLockedAppearance() {
            with(binding) {
                // 未解鎖狀態
                tvAchievementStatus.text = "未解鎖"
                tvAchievementStatus.setTextColor(root.context.getColor(R.color.text_tertiary))

                // 隱藏解鎖時間
                tvUnlockedDate.visibility = View.GONE

                // 灰色外觀
                tvAchievementTitle.alpha = 0.6f
                tvAchievementDescription.alpha = 0.6f
                ivAchievementIcon.alpha = 0.6f
                tvAchievementPoints.setTextColor(root.context.getColor(R.color.text_tertiary))
            }
        }

        private fun getCategoryName(category: String): String {
            return when (category) {
                "task" -> "任務成就"
                "streak" -> "連續性成就"
                "module" -> "模組探索"
                "special" -> "特殊成就"
                else -> "其他成就"
            }
        }
    }

    class AchievementDiffCallback : DiffUtil.ItemCallback<Achievement>() {
        override fun areItemsTheSame(oldItem: Achievement, newItem: Achievement): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Achievement, newItem: Achievement): Boolean {
            return oldItem == newItem
        }
    }
}