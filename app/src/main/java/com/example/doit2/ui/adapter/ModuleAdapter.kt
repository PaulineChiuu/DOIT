package com.example.doit2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.doit2.R
import com.example.doit2.data.model.ModuleSetting
import com.example.doit2.databinding.ItemModuleCardBinding

class ModuleAdapter(
    private val onModuleClick: (ModuleSetting) -> Unit
) : ListAdapter<ModuleSetting, ModuleAdapter.ModuleViewHolder>(ModuleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val binding = ItemModuleCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ModuleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ModuleViewHolder(
        private val binding: ItemModuleCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(moduleSetting: ModuleSetting) {
            // 設定模組名稱和圖示
            val moduleInfo = getModuleInfo(moduleSetting.moduleName)
            binding.tvModuleName.text = moduleInfo.first
            binding.ivModuleIcon.setImageResource(moduleInfo.second)

            // 點擊事件
            binding.root.setOnClickListener {
                onModuleClick(moduleSetting)
            }
        }

        private fun getModuleInfo(moduleName: String): Pair<String, Int> {
            return when (moduleName) {
                "tasks_goal" -> "任務目標" to android.R.drawable.ic_menu_agenda
                "calendar" -> "日曆追蹤" to android.R.drawable.ic_menu_month
                "self_talk" -> "自我對話" to android.R.drawable.ic_menu_edit
                "achievements" -> "成就獎勵" to android.R.drawable.ic_menu_myplaces
                "meditation" -> "靜心倒數" to android.R.drawable.ic_menu_recent_history
                "music" -> "習慣配樂" to android.R.drawable.ic_media_play
                "pomodoro" -> "番茄鐘" to android.R.drawable.ic_menu_rotate
                else -> "未知模組" to android.R.drawable.ic_menu_info_details
            }
        }
    }

    class ModuleDiffCallback : DiffUtil.ItemCallback<ModuleSetting>() {
        override fun areItemsTheSame(oldItem: ModuleSetting, newItem: ModuleSetting): Boolean {
            return oldItem.moduleName == newItem.moduleName
        }

        override fun areContentsTheSame(oldItem: ModuleSetting, newItem: ModuleSetting): Boolean {
            return oldItem == newItem
        }
    }
}