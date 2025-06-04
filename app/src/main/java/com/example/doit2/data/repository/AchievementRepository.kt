package com.example.doit2.data.repository

import androidx.lifecycle.LiveData
import com.example.doit2.data.database.AchievementDao
import com.example.doit2.data.database.UserStatsDao
import com.example.doit2.data.model.Achievement
import com.example.doit2.data.model.UserStats
import java.util.Date

class AchievementRepository(
    private val achievementDao: AchievementDao,
    private val userStatsDao: UserStatsDao
) {

    // Achievement operations
    fun getAllAchievements(): LiveData<List<Achievement>> = achievementDao.getAllAchievements()

    fun getUnlockedAchievements(): LiveData<List<Achievement>> = achievementDao.getUnlockedAchievements()

    fun getLockedAchievements(): LiveData<List<Achievement>> = achievementDao.getLockedAchievements()

    suspend fun getAchievementById(id: String): Achievement? = achievementDao.getAchievementById(id)

    suspend fun unlockAchievement(achievementId: String): Boolean {
        val achievement = achievementDao.getAchievementById(achievementId)
        return if (achievement != null && !achievement.isUnlocked) {
            val unlockedAchievement = achievement.copy(
                isUnlocked = true,
                unlockedDate = Date()
            )
            achievementDao.updateAchievement(unlockedAchievement)
            userStatsDao.addPoints(achievement.points)
            true
        } else {
            false
        }
    }

    suspend fun initializeAchievements() {
        // 檢查是否已初始化
        val count = achievementDao.getTotalCount()
        if (count == 0) {
            val defaultAchievements = createDefaultAchievements()
            achievementDao.insertAchievements(defaultAchievements)
        }
    }

    // UserStats operations
    fun getUserStats(): LiveData<UserStats?> = userStatsDao.getUserStats()

    suspend fun getUserStatsSync(): UserStats? = userStatsDao.getUserStatsSync()

    suspend fun initializeUserStats() {
        val existing = userStatsDao.getUserStatsSync()
        if (existing == null) {
            userStatsDao.insertUserStats(UserStats())
        }
    }

    suspend fun updateUserStats(userStats: UserStats) = userStatsDao.updateUserStats(userStats)

    suspend fun addPoints(points: Int) = userStatsDao.addPoints(points)

    suspend fun incrementCompletedTasks() = userStatsDao.incrementCompletedTasks()

    suspend fun incrementTodayCompletedTasks() = userStatsDao.incrementTodayCompletedTasks()

    suspend fun addMeditationMinutes(minutes: Int) = userStatsDao.addMeditationMinutes(minutes)

    suspend fun incrementMusicUsage() = userStatsDao.incrementMusicUsage()

    suspend fun incrementPomodoroSessions() = userStatsDao.incrementPomodoroSessions()

    private fun createDefaultAchievements(): List<Achievement> {
        return listOf(
            // 任務相關成就
            Achievement(
                id = "first_task",
                title = "初試啼聲",
                description = "完成你的第一個任務",
                iconRes = android.R.drawable.ic_menu_agenda,
                points = 10,
                category = "task"
            ),
            Achievement(
                id = "daily_5_tasks",
                title = "效率達人",
                description = "單日完成5個任務",
                iconRes = android.R.drawable.ic_menu_today,
                points = 50,
                category = "task"
            ),
            Achievement(
                id = "total_50_tasks",
                title = "任務大師",
                description = "累計完成50個任務",
                iconRes = android.R.drawable.ic_menu_manage,
                points = 200,
                category = "task"
            ),

            // 連續性成就
            Achievement(
                id = "streak_3_days",
                title = "堅持不懈",
                description = "連續3天使用App",
                iconRes = android.R.drawable.ic_menu_recent_history,
                points = 30,
                category = "streak"
            ),
            Achievement(
                id = "streak_7_days",
                title = "習慣養成",
                description = "連續7天完成任務",
                iconRes = android.R.drawable.ic_menu_week,
                points = 100,
                category = "streak"
            ),
            Achievement(
                id = "streak_30_days",
                title = "超級堅持",
                description = "連續30天打卡",
                iconRes = android.R.drawable.ic_menu_month,
                points = 500,
                category = "streak"
            ),

            // 模組探索成就
            Achievement(
                id = "meditation_first",
                title = "靜心初體驗",
                description = "首次使用靜心倒數",
                iconRes = android.R.drawable.ic_menu_compass,
                points = 20,
                category = "module"
            ),
            Achievement(
                id = "music_first",
                title = "音樂愛好者",
                description = "首次使用習慣配樂",
                iconRes = android.R.drawable.ic_media_play,
                points = 20,
                category = "module"
            ),
            Achievement(
                id = "all_modules",
                title = "全能選手",
                description = "解鎖所有模組",
                iconRes = android.R.drawable.ic_menu_gallery,
                points = 100,
                category = "module"
            ),

            // 特殊成就
            Achievement(
                id = "meditation_1_hour",
                title = "靜心大師",
                description = "靜心累計1小時",
                iconRes = android.R.drawable.ic_menu_camera,
                points = 150,
                category = "special"
            ),
            Achievement(
                id = "night_owl",
                title = "夜貓子",
                description = "深夜完成任務 (23:00後)",
                iconRes = android.R.drawable.ic_dialog_info,
                points = 25,
                category = "special"
            ),
            Achievement(
                id = "daily_all_modules",
                title = "多才多藝",
                description = "一天內使用所有模組",
                iconRes = android.R.drawable.ic_menu_set_as,
                points = 80,
                category = "special"
            ),
            Achievement(
                id = "pomodoro_first",
                title = "番茄初體驗",
                description = "完成第一個番茄鐘專注時間",
                iconRes = android.R.drawable.ic_menu_recent_history,
                points = 20,
                category = "pomodoro"
            ),
            Achievement(
                id = "pomodoro_10_sessions",
                title = "專注新手",
                description = "完成10個番茄鐘專注時間",
                iconRes = android.R.drawable.ic_menu_agenda,
                points = 100,
                category = "pomodoro"
            ),
            Achievement(
                id = "pomodoro_master",
                title = "番茄大師",
                description = "完成50個番茄鐘專注時間",
                iconRes = android.R.drawable.ic_menu_manage,
                points = 300,
                category = "pomodoro"
            ),
            Achievement(
                id = "daily_5_pomodoros",
                title = "今日專注王",
                description = "單日完成5個番茄鐘",
                iconRes = android.R.drawable.ic_menu_today,
                points = 80,
                category = "pomodoro"
            )
        )
    }
}