package com.example.doit2

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doit2.data.model.Task
import com.example.doit2.databinding.ActivityMainBinding
import com.example.doit2.ui.adapter.TaskAdapter
import com.example.doit2.ui.dialog.AddTaskDialog
import com.example.doit2.ui.viewmodel.TaskViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val taskViewModel: TaskViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { task ->
                // 點擊任務項目 - 可以導航到詳情或直接編輯
                showEditTaskDialog(task)
            },
            onTaskToggle = { task ->
                // 切換任務完成狀態
                taskViewModel.updateTask(task)
                showTaskStatusToast(task.isCompleted)
            },
            onTaskEdit = { task ->
                // 編輯任務
                showEditTaskDialog(task)
            },
            onTaskDelete = { task ->
                // 刪除任務
                showDeleteConfirmDialog(task)
            }
        )

        binding.rvTasks.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = taskAdapter
        }
    }

    private fun setupObservers() {
        // 觀察所有任務
        taskViewModel.allTasks.observe(this) { tasks ->
            taskAdapter.submitList(tasks)
            updateUI(tasks)
        }
    }

    private fun setupClickListeners() {
        // 新增任務按鈕
        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }
    }

    private fun updateUI(tasks: List<Task>) {
        val totalTasks = tasks.size
        val completedTasks = tasks.count { it.isCompleted }
        val pendingTasks = totalTasks - completedTasks

        // 更新統計數據
        binding.apply {
            tvTotalTasks.text = totalTasks.toString()
            tvCompletedTasks.text = completedTasks.toString()
            tvPendingTasks.text = pendingTasks.toString()
        }

        // 顯示/隱藏空狀態
        if (tasks.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvTasks.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvTasks.visibility = View.VISIBLE
        }
    }

    private fun showAddTaskDialog() {
        val dialog = AddTaskDialog.newInstance { task ->
            taskViewModel.insertTask(task)
            Toast.makeText(this, R.string.toast_task_added, Toast.LENGTH_SHORT).show()
        }
        dialog.show(supportFragmentManager, "AddTaskDialog")
    }

    private fun showEditTaskDialog(task: Task) {
        val dialog = AddTaskDialog.newInstance(task) { updatedTask ->
            taskViewModel.updateTask(updatedTask)
            Toast.makeText(this, R.string.toast_task_updated, Toast.LENGTH_SHORT).show()
        }
        dialog.show(supportFragmentManager, "EditTaskDialog")
    }

    private fun showDeleteConfirmDialog(task: Task) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_task_title)
            .setMessage(R.string.dialog_delete_task_message)
            .setPositiveButton(R.string.btn_delete) { _, _ ->
                taskViewModel.deleteTask(task)
                Toast.makeText(this, R.string.toast_task_deleted, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun showDeleteAllConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_all_tasks_title)
            .setMessage(R.string.dialog_delete_all_tasks_message)
            .setPositiveButton(R.string.btn_delete) { _, _ ->
                taskViewModel.deleteAllTasks()
                Toast.makeText(this, R.string.toast_all_tasks_deleted, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun showTaskStatusToast(isCompleted: Boolean) {
        val messageRes = if (isCompleted) {
            R.string.toast_task_completed
        } else {
            R.string.toast_task_uncompleted
        }
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_all -> {
                showDeleteAllConfirmDialog()
                true
            }
            R.id.action_settings -> {
                // TODO: 實作設定頁面
                Toast.makeText(this, "設定功能即將推出", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}