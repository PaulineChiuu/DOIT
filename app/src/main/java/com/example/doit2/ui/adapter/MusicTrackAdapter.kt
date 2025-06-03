package com.example.doit2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.doit2.MusicActivity
import com.example.doit2.R
import com.example.doit2.databinding.ItemMusicTrackBinding

class MusicTrackAdapter(
    private val tracks: List<MusicActivity.MusicTrack>,
    private val onTrackClick: (MusicActivity.MusicTrack) -> Unit
) : RecyclerView.Adapter<MusicTrackAdapter.MusicTrackViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicTrackViewHolder {
        val binding = ItemMusicTrackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MusicTrackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MusicTrackViewHolder, position: Int) {
        holder.bind(tracks[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = tracks.size

    fun setSelectedTrack(track: MusicActivity.MusicTrack) {
        val newPosition = tracks.indexOfFirst { it.name == track.name }
        if (newPosition != -1) {
            val oldPosition = selectedPosition
            selectedPosition = newPosition

            if (oldPosition != -1) {
                notifyItemChanged(oldPosition)
            }
            notifyItemChanged(selectedPosition)
        }
    }

    fun clearSelection() {
        val oldPosition = selectedPosition
        selectedPosition = -1
        if (oldPosition != -1) {
            notifyItemChanged(oldPosition)
        }
    }

    inner class MusicTrackViewHolder(
        private val binding: ItemMusicTrackBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(track: MusicActivity.MusicTrack, isSelected: Boolean) {
            binding.tvTrackName.text = track.name
            binding.tvTrackDescription.text = track.description
            binding.ivTrackIcon.setImageResource(getTrackIcon(track.name))

            // 設定選中狀態的視覺效果
            if (isSelected) {
                binding.root.setCardBackgroundColor(binding.root.context.getColor(R.color.primary_color))
                binding.tvTrackName.setTextColor(binding.root.context.getColor(R.color.white))
                binding.tvTrackDescription.setTextColor(binding.root.context.getColor(R.color.white))
                binding.ivTrackIcon.setColorFilter(binding.root.context.getColor(R.color.white))
            } else {
                binding.root.setCardBackgroundColor(binding.root.context.getColor(R.color.card_background))
                binding.tvTrackName.setTextColor(binding.root.context.getColor(R.color.text_primary))
                binding.tvTrackDescription.setTextColor(binding.root.context.getColor(R.color.text_secondary))
                binding.ivTrackIcon.clearColorFilter()
            }

            binding.root.setOnClickListener {
                onTrackClick(track)
            }
        }

        private fun getTrackIcon(trackName: String): Int {
            return when (trackName) {
                "專注音樂" -> android.R.drawable.ic_media_play
                "放鬆音樂" -> android.R.drawable.ic_media_pause
                "自然音效" -> android.R.drawable.ic_menu_compass
                "雨聲" -> android.R.drawable.ic_dialog_info
                else -> android.R.drawable.ic_media_play
            }
        }
    }
}