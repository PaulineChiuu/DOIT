package com.example.doit2

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doit2.databinding.ActivityTaskGoalBinding
import com.example.doit2.ui.adapter.TaskGoalAdapter
import com.example.doit2.ui.dialog.AddTaskGoalDialog
import java.text.SimpleDateFormat
import java.util.*

class TaskGoalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskGoalBinding
    private lateinit var taskGoalAdapter: TaskGoalAdapter
    private val taskGoalList = mutableListOf<TaskGoal>()
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("TaskGoalActivity", "=== 任務目標 Activity 開始 ===")

        binding = ActivityTaskGoalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        loadTaskGoals()
        updateUI()

        android.util.Log.d("TaskGoalActivity", "=== 任務目標 Activity 完成 ===")
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "🎯 任務目標"
    }

    private fun setupRecyclerView() {
        taskGoalAdapter = TaskGoalAdapter(
            onToggleComplete = { goal ->
                toggleGoalComplete(goal)
            },
            onEditClick = { goal ->
                showEditDialog(goal)
            },
            onDeleteClick = { goal ->
                showDeleteDialog(goal)
            },
            onProgressUpdate = { goal, progress ->
                updateGoalProgress(goal, progress)
            }
        )

        binding.rvTaskGoals.apply {
            layoutManager = LinearLayoutManager(this@TaskGoalActivity)
            adapter = taskGoalAdapter
        }
    }

    private fun setupClickListeners() {
        // 新增目標按鈕
        binding.fabAddGoal.setOnClickListener {
            showAddDialog()
        }

        // 分類篩選按鈕 - 使用 ToggleButton
        setupToggleGroupBehavior()

        // 清除已完成按鈕
        binding.btnClearCompleted.setOnClickListener {
            showClearCompletedDialog()
        }
    }

    private fun setupToggleGroupBehavior() {
        val toggleButtons = listOf(
            binding.chipAll to null,
            binding.chipWork to TaskGoalCategory.WORK,
            binding.chipPersonal to TaskGoalCategory.PERSONAL,
            binding.chipHealth to TaskGoalCategory.HEALTH,
            binding.chipLearning to TaskGoalCategory.LEARNING
        )

        toggleButtons.forEach { (button, category) ->
            button.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // 取消其他按鈕的選中狀態
                    toggleButtons.forEach { (otherButton, _) ->
                        if (otherButton != button) {
                            otherButton.isChecked = false
                        }
                    }
                    // 篩選目標
                    filterGoals(category)
                } else {
                    // 如果取消選中，檢查是否有其他按鈕被選中
                    val anyChecked = toggleButtons.any { (btn, _) -> btn.isChecked }
                    if (!anyChecked) {
                        // 如果沒有任何按鈕被選中，預設選中「全部」
                        binding.chipAll.isChecked = true
                        filterGoals(null)
                    }
                }
            }
        }
    }

    private fun loadTaskGoals() {
        // 從 SharedPreferences 載入數據
        val prefs = getSharedPreferences("task_goals_data", MODE_PRIVATE)

        try {
            taskGoalList.clear()

            // 載入示例數據（首次使用）
            val hasData = prefs.getBoolean("has_data", false)
            if (!hasData) {
                taskGoalList.addAll(getDefaultGoals())
                saveTaskGoals()
                prefs.edit().putBoolean("has_data", true).apply()
            } else {
                loadSavedGoals()
            }

            android.util.Log.d("TaskGoalActivity", "載入 ${taskGoalList.size} 個目標")
        } catch (e: Exception) {
            android.util.Log.e("TaskGoalActivity", "載入目標失敗", e)
            taskGoalList.addAll(getDefaultGoals())
        }
    }

    private fun loadSavedGoals() {
        val prefs = getSharedPreferences("task_goals_data", MODE_PRIVATE)
        val goalCount = prefs.getInt("goal_count", 0)

        for (i in 0 until goalCount) {
            try {
                val goal = TaskGoal(
                    id = prefs.getLong("goal_${i}_id", 0),
                    title = prefs.getString("goal_${i}_title", "") ?: "",
                    description = prefs.getString("goal_${i}_description", "") ?: "",
                    category = TaskGoalCategory.valueOf(prefs.getString("goal_${i}_category", "PERSONAL") ?: "PERSONAL"),
                    priority = TaskGoalPriority.valueOf(prefs.getString("goal_${i}_priority", "MEDIUM") ?: "MEDIUM"),
                    targetDate = Date(prefs.getLong("goal_${i}_target_date", System.currentTimeMillis())),
                    progress = prefs.getInt("goal_${i}_progress", 0),
                    isCompleted = prefs.getBoolean("goal_${i}_completed", false),
                    createdAt = Date(prefs.getLong("goal_${i}_created", System.currentTimeMillis()))
                )
                taskGoalList.add(goal)
            } catch (e: Exception) {
                android.util.Log.e("TaskGoalActivity", "載入目標 $i 失敗", e)
            }
        }
    }

    private fun getDefaultGoals(): List<TaskGoal> {
        val calendar = Calendar.getInstance()

        return listOf(
            TaskGoal(
                id = 1,
                title = "完成專案報告",
                description = "完成本季度的工作專案報告，包含進度分析和未來規劃",
                category = TaskGoalCategory.WORK,
                priority = TaskGoalPriority.HIGH,
                targetDate = Date(calendar.timeInMillis + 7 * 24 * 60 * 60 * 1000), // 一週後
                progress = 60,
                createdAt = Date()
            ),
            TaskGoal(
                id = 2,
                title = "學會新的程式語言",
                description = "掌握 Kotlin Android 開發，能獨立完成 App 專案",
                category = TaskGoalCategory.LEARNING,
                priority = TaskGoalPriority.MEDIUM,
                targetDate = Date(calendar.timeInMillis + 30 * 24 * 60 * 60 * 1000), // 一個月後
                progress = 40,
                createdAt = Date(System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000)
            ),
            TaskGoal(
                id = 3,
                title = "每週運動3次",
                description = "建立規律運動習慣，每週至少運動3次，每次30分鐘以上",
                category = TaskGoalCategory.HEALTH,
                priority = TaskGoalPriority.HIGH,
                targetDate = Date(calendar.timeInMillis + 90 * 24 * 60 * 60 * 1000), // 三個月後
                progress = 25,
                createdAt = Date(System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000)
            ),
            TaskGoal(
                id = 4,
                title = "整理房間",
                description = "清理和整理個人房間，建立更好的生活環境",
                category = TaskGoalCategory.PERSONAL,
                priority = TaskGoalPriority.LOW,
                targetDate = Date(calendar.timeInMillis + 3 * 24 * 60 * 60 * 1000), // 三天後
                progress = 80,
                createdAt = Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000)
            )
        )
    }

    private fun saveTaskGoals() {
        val prefs = getSharedPreferences("task_goals_data", MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putInt("goal_count", taskGoalList.size)

        taskGoalList.forEachIndexed { index, goal ->
            editor.putLong("goal_${index}_id", goal.id)
            editor.putString("goal_${index}_title", goal.title)
            editor.putString("goal_${index}_description", goal.description)
            editor.putString("goal_${index}_category", goal.category.name)
            editor.putString("goal_${index}_priority", goal.priority.name)
            editor.putLong("goal_${index}_target_date", goal.targetDate.time)
            editor.putInt("goal_${index}_progress", goal.progress)
            editor.putBoolean("goal_${index}_completed", goal.isCompleted)
            editor.putLong("goal_${index}_created", goal.createdAt.time)
        }

        editor.apply()
        android.util.Log.d("TaskGoalActivity", "保存 ${taskGoalList.size} 個目標")
    }

    private fun showAddDialog() {
        AddTaskGoalDialog.newInstance(this) { title, description, category, priority, targetDate ->
            addTaskGoal(title, description, category, priority, targetDate)
        }
    }

    private fun showEditDialog(goal: TaskGoal) {
        AddTaskGoalDialog.newInstance(this, goal) { title, description, category, priority, targetDate ->
            editTaskGoal(goal.id, title, description, category, priority, targetDate)
        }
    }

    private fun showDeleteDialog(goal: TaskGoal) {
        AlertDialog.Builder(this)
            .setTitle("刪除目標")
            .setMessage("確定要刪除「${goal.title}」嗎？")
            .setPositiveButton("刪除") { _, _ ->
                deleteTaskGoal(goal)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showClearCompletedDialog() {
        val completedCount = taskGoalList.count { it.isCompleted }
        if (completedCount == 0) {
            Toast.makeText(this, "沒有已完成的目標可以清除", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("清除已完成目標")
            .setMessage("確定要清除 $completedCount 個已完成的目標嗎？")
            .setPositiveButton("清除") { _, _ ->
                clearCompletedGoals()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun addTaskGoal(title: String, description: String, category: TaskGoalCategory, priority: TaskGoalPriority, targetDate: Date) {
        val newGoal = TaskGoal(
            id = System.currentTimeMillis(),
            title = title,
            description = description,
            category = category,
            priority = priority,
            targetDate = targetDate,
            createdAt = Date()
        )

        taskGoalList.add(0, newGoal) // 新的放在最前面
        saveTaskGoals()
        updateUI()

        Toast.makeText(this, "目標已新增", Toast.LENGTH_SHORT).show()
        android.util.Log.d("TaskGoalActivity", "新增目標: $title")
    }

    private fun editTaskGoal(id: Long, title: String, description: String, category: TaskGoalCategory, priority: TaskGoalPriority, targetDate: Date) {
        val index = taskGoalList.indexOfFirst { it.id == id }
        if (index != -1) {
            taskGoalList[index] = taskGoalList[index].copy(
                title = title,
                description = description,
                category = category,
                priority = priority,
                targetDate = targetDate
            )
            saveTaskGoals()
            updateUI()
            Toast.makeText(this, "目標已更新", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteTaskGoal(goal: TaskGoal) {
        taskGoalList.remove(goal)
        saveTaskGoals()
        updateUI()
        Toast.makeText(this, "目標已刪除", Toast.LENGTH_SHORT).show()
    }

    private fun toggleGoalComplete(goal: TaskGoal) {
        val index = taskGoalList.indexOfFirst { it.id == goal.id }
        if (index != -1) {
            val updatedGoal = taskGoalList[index].copy(
                isCompleted = !goal.isCompleted,
                progress = if (!goal.isCompleted) 100 else goal.progress
            )
            taskGoalList[index] = updatedGoal
            saveTaskGoals()
            updateUI()

            val message = if (updatedGoal.isCompleted) "🎉 目標完成！" else "目標重新啟用"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateGoalProgress(goal: TaskGoal, progress: Int) {
        val index = taskGoalList.indexOfFirst { it.id == goal.id }
        if (index != -1) {
            taskGoalList[index] = taskGoalList[index].copy(
                progress = progress,
                isCompleted = progress >= 100
            )
            saveTaskGoals()
            updateUI()
        }
    }

    private fun filterGoals(category: TaskGoalCategory?) {
        val filteredList = if (category == null) {
            taskGoalList
        } else {
            taskGoalList.filter { it.category == category }
        }
        taskGoalAdapter.submitList(filteredList)
        updateStats(filteredList)
    }

    private fun clearCompletedGoals() {
        taskGoalList.removeAll { it.isCompleted }
        saveTaskGoals()
        updateUI()
        Toast.makeText(this, "已清除完成的目標", Toast.LENGTH_SHORT).show()
    }

    private fun updateUI() {
        // 更新列表
        taskGoalAdapter.submitList(taskGoalList.toList())
        updateStats(taskGoalList)

        // 顯示/隱藏空狀態
        if (taskGoalList.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.rvTaskGoals.visibility = View.GONE
            binding.btnClearCompleted.isEnabled = false
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.rvTaskGoals.visibility = View.VISIBLE
            binding.btnClearCompleted.isEnabled = taskGoalList.any { it.isCompleted }
        }
    }

    private fun updateStats(goals: List<TaskGoal>) {
        val totalGoals = goals.size
        val completedGoals = goals.count { it.isCompleted }
        val inProgressGoals = totalGoals - completedGoals
        val avgProgress = if (goals.isNotEmpty()) goals.sumOf { it.progress } / goals.size else 0

        binding.tvTotalGoals.text = totalGoals.toString()
        binding.tvCompletedGoals.text = completedGoals.toString()
        binding.tvInProgressGoals.text = inProgressGoals.toString()
        binding.tvAvgProgress.text = "$avgProgress%"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

/**
 * 任務目標
 */
data class TaskGoal(
    val id: Long,
    val title: String,
    val description: String,
    val category: TaskGoalCategory,
    val priority: TaskGoalPriority,
    val targetDate: Date,
    val progress: Int = 0,
    val isCompleted: Boolean = false,
    val createdAt: Date
)

/**
 * 目標分類
 */
enum class TaskGoalCategory(val displayName: String, val emoji: String, val color: String) {
    WORK("工作", "💼", "#2196F3"),
    PERSONAL("個人", "🏠", "#4CAF50"),
    HEALTH("健康", "💪", "#FF9800"),
    LEARNING("學習", "📚", "#9C27B0")
}

/**
 * 優先級
 */
enum class TaskGoalPriority(val displayName: String, val color: String) {
    HIGH("高", "#F44336"),
    MEDIUM("中", "#FF9800"),
    LOW("低", "#4CAF50")
}