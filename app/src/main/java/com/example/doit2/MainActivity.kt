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
import com.example.doit2.data.model.ModuleSetting
import com.example.doit2.data.model.Task
import com.example.doit2.databinding.ActivityMainBinding
import com.example.doit2.ui.adapter.TaskAdapter
import com.example.doit2.ui.dialog.AddTaskDialog
import com.example.doit2.ui.viewmodel.ModuleSettingViewModel
import com.example.doit2.ui.viewmodel.TaskViewModel
import com.example.doit2.ui.adapter.ModuleAdapter
import android.content.Intent
import com.example.doit2.ui.viewmodel.AchievementViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val taskViewModel: TaskViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter
    private val moduleSettingViewModel: ModuleSettingViewModel by viewModels()
    private lateinit var moduleAdapter: ModuleAdapter
    private val achievementViewModel: AchievementViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupModuleRecyclerView()
        initializeModules()
        forceInitializeModules()
        setupObservers()
        setupClickListeners()
        initializeAchievementSystem()
    }

    private fun forceInitializeModules() {
        lifecycleScope.launch {
            android.util.Log.d("MainActivity", "強制初始化模組")

            // 強制插入所有預設模組
            val defaultModules = listOf(
                ModuleSetting("tasks_goal", false, 0),
                ModuleSetting("calendar", false, 1),
                ModuleSetting("self_talk", false, 2),
                ModuleSetting("achievements", false, 3),
                ModuleSetting("meditation", false, 4),
                ModuleSetting("music", false, 5),
                ModuleSetting("pomodoro", false, 6)
            )

            defaultModules.forEach { module ->
                try {
                    moduleSettingViewModel.updateModuleSetting(module)
                    android.util.Log.d("MainActivity", "插入模組: ${module.moduleName}")
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "插入模組失敗: ${module.moduleName}", e)
                }
            }

            // 啟用日曆模組來測試顯示
            try {
                moduleSettingViewModel.updateModuleEnabled("calendar", true)
                android.util.Log.d("MainActivity", "啟用成就模組")
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "啟用模組失敗", e)
            }
        }
    }


    private fun setupModuleRecyclerView() {
        moduleAdapter = ModuleAdapter { moduleSetting ->
            // 點擊模組卡片的處理
            onModuleCardClick(moduleSetting)
        }

        binding.rvModules.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = moduleAdapter
        }
    }

    private fun initializeModules() {
        // 手動觸發模組初始化
        moduleSettingViewModel.allModuleSettings.observe(this) { modules ->
            // 這會觸發 ViewModel 的初始化
        }
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

        moduleSettingViewModel.enabledModuleSettings.observe(this) { enabledModules ->
            android.util.Log.d("MainActivity", "啟用的模組數量: ${enabledModules.size}")
            enabledModules.forEach { module ->
                android.util.Log.d("MainActivity", "模組: ${module.moduleName}, 啟用: ${module.isEnabled}")
            }
            updateModulesUI(enabledModules)
        }
    }

    private fun initializeAchievementSystem() {
        // 初始化成就系統
        taskViewModel.getAchievementManager().initialize()

        // 檢查模組使用（如果成就模組已啟用）
        achievementViewModel.getAchievementManager().checkModuleUsage("achievements")
    }

    private fun updateModulesUI(enabledModules: List<ModuleSetting>) {
        if (enabledModules.isNotEmpty()) {
            binding.layoutModulesSection.visibility = View.VISIBLE
            moduleAdapter.submitList(enabledModules)
        } else {
            binding.layoutModulesSection.visibility = View.GONE
        }
    }


    private fun setupClickListeners() {
        // 新增任務按鈕
        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        binding.tvManageModules.setOnClickListener {
            val intent = Intent(this, ModuleSettingsActivity::class.java)
            startActivity(intent)
        }


    }

    private fun onModuleCardClick(moduleSetting: ModuleSetting) {
        when (moduleSetting.moduleName) {
            "tasks_goal" -> {
                // 跳轉到任務目標頁面
                val intent = Intent(this, TaskGoalActivity::class.java)
                startActivity(intent)
                achievementViewModel.getAchievementManager().checkModuleUsage("tasks_goal")
            }
            "calendar" -> {
                // 跳轉到日曆頁面
                val intent = Intent(this, CalendarActivity::class.java)
                startActivity(intent)
                // 記錄模組使用
                achievementViewModel.getAchievementManager().checkModuleUsage("calendar")
            }
            "self_talk" -> {
                // 跳轉到自我對話頁面
                val intent = Intent(this, SelfTalkActivity::class.java)
                startActivity(intent)
                achievementViewModel.getAchievementManager().checkModuleUsage("self_talk")
            }
            "achievements" -> {
                // 跳轉到成就頁面
                val intent = Intent(this, AchievementsActivity::class.java)
                startActivity(intent)
                // 記錄模組使用
                achievementViewModel.getAchievementManager().checkModuleUsage("achievements")
            }
            "meditation" -> {
                achievementViewModel.getAchievementManager().checkModuleUsage("meditation")
                // 跳轉到靜心頁面
                val intent = Intent(this, MeditationActivity::class.java)
                startActivity(intent)
            }
            "music" -> {
                achievementViewModel.getAchievementManager().checkModuleUsage("music")
                val intent = Intent(this, MusicActivity::class.java)
                startActivity(intent)
            }
            "pomodoro" -> {
                // 跳轉到番茄鐘頁面
                val intent = Intent(this, PomodoroActivity::class.java)
                startActivity(intent)
                // 記錄模組使用
                achievementViewModel.getAchievementManager().checkModuleUsage("pomodoro")
            }
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
                Toast.makeText(this, "設定功能即將推出", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_tour_map -> {
                val intent = Intent(this, TaiwanTourMapActivity::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}