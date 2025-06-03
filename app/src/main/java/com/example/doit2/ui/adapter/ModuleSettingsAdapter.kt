package com.example.doit2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.doit2.R
import com.example.doit2.data.model.ModuleSetting
import com.example.doit2.databinding.ItemModuleSettingBinding

class ModuleSettingsAdapter(
    private val onModuleToggle: (ModuleSetting, Boolean) -> Unit
) : ListAdapter<ModuleSetting, ModuleSettingsAdapter.ModuleSettingViewHolder>(ModuleSettingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleSettingViewHolder {
        val binding = ItemModuleSettingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ModuleSettingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ModuleSettingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ModuleSettingViewHolder(
        private val binding: ItemModuleSettingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(moduleSetting: ModuleSetting) {
            val moduleInfo = getModuleInfo(moduleSetting.moduleName)

            // 設定模組資訊
            binding.tvModuleName.text = moduleInfo.name
            binding.tvModuleDescription.text = moduleInfo.description
            binding.ivModuleIcon.setImageResource(moduleInfo.icon)

            // 設定開關狀態（暫時移除監聽器避免觸發回調）
            binding.switchModule.setOnCheckedChangeListener(null)
            binding.switchModule.isChecked = moduleSetting.isEnabled

            // 設定開關監聽器
            binding.switchModule.setOnCheckedChangeListener { _, isChecked ->
                onModuleToggle(moduleSetting, isChecked)
            }

            // 整個卡片點擊也可以切換開關
            binding.root.setOnClickListener {
                binding.switchModule.toggle()
            }
        }

        private fun getModuleInfo(moduleName: String): ModuleInfo {
            return when (moduleName) {
                "tasks_goal" -> ModuleInfo(
                    name = "任務目標",
                    description = "設定具體目標並追蹤進度",
                    icon = android.R.drawable.ic_menu_agenda
                )
                "calendar" -> ModuleInfo(
                    name = "日曆追蹤",
                    description = "查看任務完成的日曆視圖",
                    icon = android.R.drawable.ic_menu_month
                )
                "self_talk" -> ModuleInfo(
                    name = "自我對話",
                    description = "記錄想法和反思心得",
                    icon = android.R.drawable.ic_menu_edit
                )
                "achievements" -> ModuleInfo(
                    name = "成就獎勵",
                    description = "完成任務獲得積分和獎勵",
                    icon = android.R.drawable.ic_menu_preferences
                )
                "meditation" -> ModuleInfo(
                    name = "靜心倒數",
                    description = "任務前的冥想和放鬆",
                    icon = android.R.drawable.ic_menu_recent_history
                )
                "music" -> ModuleInfo(
                    name = "習慣配樂",
                    description = "專注工作的背景音樂",
                    icon = android.R.drawable.ic_media_play
                )
                else -> ModuleInfo(
                    name = "未知模組",
                    description = "未知的功能模組",
                    icon = android.R.drawable.ic_menu_info_details
                )
            }
        }
    }

    data class ModuleInfo(
        val name: String,
        val description: String,
        val icon: Int
    )

    class ModuleSettingDiffCallback : DiffUtil.ItemCallback<ModuleSetting>() {
        override fun areItemsTheSame(oldItem: ModuleSetting, newItem: ModuleSetting): Boolean {
            return oldItem.moduleName == newItem.moduleName
        }

        override fun areContentsTheSame(oldItem: ModuleSetting, newItem: ModuleSetting): Boolean {
            return oldItem == newItem
        }
    }
}