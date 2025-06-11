package com.example.doit2.utils

import android.content.Context
import android.media.MediaPlayer
import android.widget.Toast
import com.example.doit2.data.model.UserStats
import com.example.doit2.data.repository.AchievementRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AchievementManager(
    private val context: Context,
    private val repository: AchievementRepository,
    private val onAchievementUnlocked: ((achievementId: String, title: String, points: Int) -> Unit)? = null
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * 任務完成時調用 - 只在任務首次完成時調用
     */
    fun checkTaskCompletion() {
        CoroutineScope(Dispatchers.IO).launch {
            val userStats = repository.getUserStatsSync() ?: return@launch

            // 重要：先更新統計，再檢查成就
            repository.incrementCompletedTasks()
            repository.incrementTodayCompletedTasks()

            // 獲取更新後的統計數據
            val updatedStats = repository.getUserStatsSync() ?: return@launch
            val newCompletedTasks = updatedStats.completedTasks
            val newTodayTasks = updatedStats.todayCompletedTasks

            // 檢查任務相關成就
            checkTaskAchievements(newCompletedTasks, newTodayTasks)

            // 檢查深夜完成任務成就
            checkNightOwlAchievement()

            // 更新等級
            updateUserLevel()
        }
    }

    /**
     * 檢查任務相關成就
     */
    private suspend fun checkTaskAchievements(totalTasks: Int, todayTasks: Int) {
        when {
            totalTasks == 1 -> unlockAchievement("first_task")
            todayTasks == 5 -> unlockAchievement("daily_5_tasks")
            totalTasks == 50 -> unlockAchievement("total_50_tasks")
        }
        // 每日任務成就檢查
        when (todayTasks) {
            5 -> unlockAchievement("daily_5_tasks")
        }
    }

    /**
     * 檢查深夜完成任務成就 (23:00後)
     */
    private suspend fun checkNightOwlAchievement() {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (currentHour >= 23) {
            unlockAchievement("night_owl")
        }

    }

    /**
     * 檢查每日連續性成就 - 在 App 啟動時調用
     */
    fun checkDailyStreak() {
        CoroutineScope(Dispatchers.IO).launch {
            val userStats = repository.getUserStatsSync() ?: return@launch
            val today = dateFormat.format(Date())
            val lastActiveDate = dateFormat.format(userStats.lastActiveDate)

            // 如果今天已經記錄過，就不用重複檢查
            if (lastActiveDate == today) return@launch

            val newStreak = when {
                isConsecutiveDay(lastActiveDate, today) -> userStats.currentStreak + 1
                else -> 1 // 重新開始
            }

            val updatedStats = userStats.copy(
                currentStreak = newStreak,
                longestStreak = maxOf(newStreak, userStats.longestStreak),
                lastActiveDate = Date(),
                todayCompletedTasks = 0, // 重置今日任務數
                todayActiveDate = Date()
            )

            repository.updateUserStats(updatedStats)
            checkStreakAchievements(newStreak)
        }
    }

    /**
     * 檢查連續性成就
     */
    private suspend fun checkStreakAchievements(streak: Int) {
        when (streak) {
            3 -> unlockAchievement("streak_3_days")
            7 -> unlockAchievement("streak_7_days")
            30 -> unlockAchievement("streak_30_days")
        }
    }

    /**
     * 檢查模組使用成就
     */
    fun checkModuleUsage(moduleId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            when (moduleId) {
                "meditation" -> {
                    unlockAchievement("meditation_first")
                }
                "music" -> {
                    unlockAchievement("music_first")
                    repository.incrementMusicUsage()
                }
                "pomodoro" -> {
                    unlockAchievement("pomodoro_module_first")
                }
                "journey" -> {
                    unlockAchievement("journey_module_first")
                }
            }


            // 記錄模組使用
            recordModuleUsage(moduleId)

            // 檢查是否解鎖所有模組
            checkAllModulesUnlocked()
        }
    }

    /**
     * 檢查靜心時間成就
     */
    fun checkMeditationTime(minutes: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.addMeditationMinutes(minutes)
            val userStats = repository.getUserStatsSync() ?: return@launch

            if (userStats.meditationMinutes + minutes >= 60) {
                unlockAchievement("meditation_1_hour")
            }
        }
    }

    /**
     * 記錄模組使用
     */
    private suspend fun recordModuleUsage(moduleId: String) {
        val userStats = repository.getUserStatsSync() ?: return
        val unlockedModules = userStats.modulesUnlocked.split(",").toMutableSet()
        unlockedModules.add(moduleId)

        val updatedStats = userStats.copy(
            modulesUnlocked = unlockedModules.filter { it.isNotEmpty() }.joinToString(",")
        )
        repository.updateUserStats(updatedStats)
    }

    /**
     * 檢查是否解鎖所有模組
     */
    private suspend fun checkAllModulesUnlocked() {
        val userStats = repository.getUserStatsSync() ?: return
        val unlockedModules = userStats.modulesUnlocked.split(",").filter { it.isNotEmpty() }

        // 檢查是否有6個模組：tasks_goal, calendar, self_talk, achievements, meditation, music
        val allModules = setOf("tasks_goal", "calendar", "self_talk", "achievements", "meditation", "music", "pomodoro", "journey")

        if (unlockedModules.toSet().containsAll(allModules)) {
            unlockAchievement("all_modules")
        }
    }

    /**
     * 解鎖成就
     */
    suspend fun unlockAchievement(achievementId: String) {
        val isNewlyUnlocked = repository.unlockAchievement(achievementId)
        if (isNewlyUnlocked) {
            val achievement = repository.getAchievementById(achievementId)
            achievement?.let {

                // 在主線程顯示 Toast
                CoroutineScope(Dispatchers.Main).launch {
                    showAchievementToast(it.title, it.points)
                }

                // 觸發回調
                onAchievementUnlocked?.invoke(achievementId, it.title, it.points)
            }

            // 更新等級
            updateUserLevel()
        }
    }

    /**
     * 更新用戶等級
     */
    private suspend fun updateUserLevel() {
        val userStats = repository.getUserStatsSync() ?: return
        val newLevel = calculateLevel(userStats.totalPoints)

        if (newLevel != userStats.currentLevel) {
            val updatedStats = userStats.copy(currentLevel = newLevel)
            repository.updateUserStats(updatedStats)

            // 等級提升通知
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(
                    context,
                    "🎉 恭喜升級到 ${getLevelName(newLevel)}！",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * 計算等級
     */
    fun calculateLevel(totalPoints: Int): Int {
        return when {
            totalPoints < 200 -> 1    // 🥉 青銅
            totalPoints < 500 -> 2    // 🥈 白銀
            totalPoints < 1000 -> 3   // 🥇 黃金
            totalPoints < 2000 -> 4   // 💎 鑽石
            else -> 5                 // 👑 傳奇
        }
    }

    /**
     * 獲取等級名稱
     */
    fun getLevelName(level: Int): String {
        return when (level) {
            1 -> "🥉 青銅"
            2 -> "🥈 白銀"
            3 -> "🥇 黃金"
            4 -> "💎 鑽石"
            5 -> "👑 傳奇"
            else -> "🥉 青銅"
        }
    }

    /**
     * 獲取下一等級所需積分
     */
    fun getPointsForNextLevel(currentPoints: Int): Int {
        return when {
            currentPoints < 200 -> 200
            currentPoints < 500 -> 500
            currentPoints < 1000 -> 1000
            currentPoints < 2000 -> 2000
            else -> 2000 // 已達最高等級
        }
    }

    /**
     * 初始化成就系統 - 在 App 啟動時調用
     */
    fun initialize() {
        CoroutineScope(Dispatchers.IO).launch {
            repository.initializeAchievements()
            repository.initializeUserStats()
            checkDailyStreak()
        }
    }

    /**
     * 顯示成就解鎖 Toast
     */
    private fun showAchievementToast(title: String, points: Int) {
        Toast.makeText(
            context,
            "🏆 解鎖成就：$title (+${points}分)",
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * 檢查是否為連續天數
     */
    private fun isConsecutiveDay(lastDateStr: String, todayStr: String): Boolean {
        return try {
            val lastDate = dateFormat.parse(lastDateStr)
            val today = dateFormat.parse(todayStr)
            val diffInMillis = today!!.time - lastDate!!.time
            val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)
            diffInDays == 1L
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 記錄番茄鐘使用
     */
    fun checkPomodoroCompletion() {
        CoroutineScope(Dispatchers.IO).launch {
            val userStats = repository.getUserStatsSync() ?: return@launch

            // 更新番茄鐘統計
            repository.incrementPomodoroSessions()

            // 獲取更新後的統計
            val updatedStats = repository.getUserStatsSync() ?: return@launch
            val totalPomodoros = updatedStats.pomodoroSessions

            // 檢查番茄鐘成就
            checkPomodoroAchievements(totalPomodoros)

            // 更新等級
            updateUserLevel()
        }
    }

    /**
     * 檢查番茄鐘相關成就
     */
    private suspend fun checkPomodoroAchievements(totalPomodoros: Int) {
        when (totalPomodoros) {
            1 -> unlockAchievement("pomodoro_first")
            10 -> unlockAchievement("pomodoro_10_sessions")
            50 -> unlockAchievement("pomodoro_master")
            100 -> unlockAchievement("pomodoro_legend")
        }
    }

}