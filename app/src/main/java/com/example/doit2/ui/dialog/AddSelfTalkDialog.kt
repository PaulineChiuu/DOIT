package com.example.doit2.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.doit2.SelfTalkEntry
import com.example.doit2.SelfTalkMood
import com.example.doit2.databinding.DialogAddSelfTalkBinding
import com.google.android.material.chip.Chip

class AddSelfTalkDialog : DialogFragment() {

    private var _binding: DialogAddSelfTalkBinding? = null
    private val binding get() = _binding!!

    private var editingEntry: SelfTalkEntry? = null
    private var selectedMood: SelfTalkMood = SelfTalkMood.THOUGHTFUL
    private var onSaveCallback: ((String, SelfTalkMood) -> Unit)? = null

    companion object {
        fun newInstance(
            entry: SelfTalkEntry? = null,
            onSave: (String, SelfTalkMood) -> Unit
        ): AddSelfTalkDialog {
            return AddSelfTalkDialog().apply {
                editingEntry = entry
                onSaveCallback = onSave
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddSelfTalkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMoodChips()
        setupButtons()

        // 如果是編輯模式，填入現有數據
        editingEntry?.let { entry ->
            binding.etContent.setText(entry.content)
            selectedMood = entry.mood
            updateMoodSelection()
            binding.tvTitle.text = "編輯對話記錄"
            binding.btnSave.text = "更新"
        }
    }

    private fun setupMoodChips() {
        binding.chipGroupMood.removeAllViews()

        SelfTalkMood.values().forEach { mood ->
            val chip = Chip(requireContext()).apply {
                text = "${mood.emoji} ${mood.label}"
                isCheckable = true
                isChecked = mood == selectedMood

                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedMood = mood
                        updateMoodSelection()
                    }
                }
            }
            binding.chipGroupMood.addView(chip)
        }
    }

    private fun updateMoodSelection() {
        for (i in 0 until binding.chipGroupMood.childCount) {
            val chip = binding.chipGroupMood.getChildAt(i) as Chip
            val mood = SelfTalkMood.values()[i]
            chip.isChecked = mood == selectedMood
        }
    }

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            val content = binding.etContent.text.toString().trim()

            if (content.isEmpty()) {
                Toast.makeText(requireContext(), "請輸入對話內容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (content.length > 500) {
                Toast.makeText(requireContext(), "內容不能超過500字", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            onSaveCallback?.invoke(content, selectedMood)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}