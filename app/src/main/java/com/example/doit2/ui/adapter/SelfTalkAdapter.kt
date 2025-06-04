package com.example.doit2.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.doit2.SelfTalkEntry
import com.example.doit2.databinding.ItemSelfTalkBinding
import java.text.SimpleDateFormat
import java.util.*

class SelfTalkAdapter(
    private val onEditClick: (SelfTalkEntry) -> Unit,
    private val onDeleteClick: (SelfTalkEntry) -> Unit
) : ListAdapter<SelfTalkEntry, SelfTalkAdapter.SelfTalkViewHolder>(SelfTalkDiffCallback()) {

    private val timeFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelfTalkViewHolder {
        val binding = ItemSelfTalkBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SelfTalkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SelfTalkViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SelfTalkViewHolder(
        private val binding: ItemSelfTalkBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: SelfTalkEntry) {
            binding.apply {
                // 內容
                tvContent.text = entry.content

                // 心情
                tvMoodEmoji.text = entry.mood.emoji
                tvMoodLabel.text = entry.mood.label

                // 時間
                tvTime.text = timeFormat.format(entry.createdAt)

                // 心情顏色
                try {
                    val color = Color.parseColor(entry.mood.color)
                    cardMood.setCardBackgroundColor(color)
                } catch (e: Exception) {
                    // 使用預設顏色
                }

                // 點擊事件
                btnEdit.setOnClickListener {
                    onEditClick(entry)
                }

                btnDelete.setOnClickListener {
                    onDeleteClick(entry)
                }

                // 長按編輯
                root.setOnLongClickListener {
                    onEditClick(entry)
                    true
                }
            }
        }
    }
}

class SelfTalkDiffCallback : DiffUtil.ItemCallback<SelfTalkEntry>() {
    override fun areItemsTheSame(oldItem: SelfTalkEntry, newItem: SelfTalkEntry): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SelfTalkEntry, newItem: SelfTalkEntry): Boolean {
        return oldItem == newItem
    }
}