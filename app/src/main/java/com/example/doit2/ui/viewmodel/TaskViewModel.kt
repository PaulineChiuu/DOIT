package com.example.doit2.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.doit2.data.database.AppDatabase
import com.example.doit2.data.model.Task
import com.example.doit2.data.repository.TaskRepository
import com.example.doit2.data.repository.AchievementRepository
import com.example.doit2.data.repository.CalendarRepository
import com.example.doit2.utils.AchievementManager
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val taskRepository = TaskRepository(database.taskDao())

    // 成就系統相關
    private val achievementRepository = AchievementRepository(
        database.achievementDao(),
        database.userStatsDao()
    )
    private val achievementManager = AchievementManager(
        context = application,
        repository = achievementRepository
    )
    private val calendarRepository = CalendarRepository(
        database.dailyRecordDao(),
        database.taskDao()
    )

    val allTasks: LiveData<List<Task>> = taskRepository.getAllTasks()
    val incompleteTasks: LiveData<List<Task>> = taskRepository.getIncompleteTasks()
    val completedTasks: LiveData<List<Task>> = taskRepository.getCompletedTasks()

    fun insertTask(task: Task) {
        viewModelScope.launch {
            taskRepository.insertTask(task)

            // 更新今日任務統計
            calendarRepository.updateTodayTaskStats()
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            val oldTask = taskRepository.getTaskById(task.id)
            taskRepository.updateTask(task)

            // 檢查是否是新完成的任務（防止重複觸發）
            val wasJustCompleted = oldTask != null &&
                    !oldTask.isCompleted &&
                    task.isCompleted

            if (wasJustCompleted) {
                // 只有在任務從未完成變為完成時才觸發成就和記錄
                achievementManager.checkTaskCompletion()
                calendarRepository.recordTaskCompletion()
            }

            // 無論如何都更新今日統計（因為可能有任務狀態變化）
            calendarRepository.updateTodayTaskStats()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)

            // 更新今日任務統計
            calendarRepository.updateTodayTaskStats()
        }
    }

    fun deleteTaskById(id: Long) {
        viewModelScope.launch {
            taskRepository.deleteTaskById(id)
        }
    }

    fun deleteAllTasks() {
        viewModelScope.launch {
            taskRepository.deleteAllTasks()
        }
    }

    // 獲取成就管理器的公開方法
    fun getAchievementManager(): AchievementManager = achievementManager
    fun getCalendarRepository(): CalendarRepository = calendarRepository
}