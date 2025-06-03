package com.example.doit2.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.doit2.R
import com.example.doit2.data.model.Task
import com.example.doit2.databinding.DialogAddTaskBinding
import com.example.doit2.utils.Constants

class AddTaskDialog(
    private val task: Task? = null,
    private val onTaskSaved: (Task) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddTaskBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddTaskBinding.inflate(LayoutInflater.from(context))

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)

        setupViews()
        setupClickListeners()

        return builder.create()
    }

    private fun setupViews() {
        // 如果是編輯模式，填入現有資料
        task?.let {
            binding.etTaskTitle.setText(it.title)
            binding.etTaskDescription.setText(it.description)
            // 更改標題為編輯任務
            // 可以考慮在佈局中加入標題 TextView 並動態更改
        }

        // 讓標題輸入框獲得焦點
        binding.etTaskTitle.requestFocus()
    }

    private fun setupClickListeners() {
        binding.apply {
            // 取消按鈕
            btnCancel.setOnClickListener {
                dismiss()
            }

            // 確認按鈕
            btnConfirm.setOnClickListener {
                saveTask()
            }
        }
    }

    private fun saveTask() {
        val title = binding.etTaskTitle.text.toString().trim()
        val description = binding.etTaskDescription.text.toString().trim()

        // 驗證輸入
        if (!validateInput(title)) {
            return
        }

        // 創建或更新任務
        val newTask = if (task == null) {
            // 新增任務
            Task(
                title = title,
                description = description
            )
        } else {
            // 編輯任務
            task.copy(
                title = title,
                description = description
            )
        }

        // 回調並關閉對話框
        onTaskSaved(newTask)
        dismiss()
    }

    private fun validateInput(title: String): Boolean {
        return when {
            title.isEmpty() -> {
                binding.etTaskTitle.error = getString(R.string.error_task_title_empty)
                binding.etTaskTitle.requestFocus()
                false
            }
            title.length > Constants.MAX_TASK_TITLE_LENGTH -> {
                binding.etTaskTitle.error = getString(R.string.error_task_title_too_long)
                binding.etTaskTitle.requestFocus()
                false
            }
            binding.etTaskDescription.text.toString().length > Constants.MAX_TASK_DESCRIPTION_LENGTH -> {
                binding.etTaskDescription.error = getString(R.string.error_task_description_too_long)
                binding.etTaskDescription.requestFocus()
                false
            }
            else -> {
                true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(task: Task? = null, onTaskSaved: (Task) -> Unit): AddTaskDialog {
            return AddTaskDialog(task, onTaskSaved)
        }
    }
}