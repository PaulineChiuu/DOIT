package com.example.doit2.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.doit2.data.database.AppDatabase
import com.example.doit2.data.model.Achievement
import com.example.doit2.data.model.UserStats
import com.example.doit2.data.repository.AchievementRepository
import com.example.doit2.utils.AchievementManager
import kotlinx.coroutines.launch

class AchievementViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = AchievementRepository(database.achievementDao(), database.userStatsDao())

    val allAchievements: LiveData<List<Achievement>> = repository.getAllAchievements()
    val unlockedAchievements: LiveData<List<Achievement>> = repository.getUnlockedAchievements()
    val lockedAchievements: LiveData<List<Achievement>> = repository.getLockedAchievements()
    val userStats: LiveData<UserStats?> = repository.getUserStats()

    private val achievementManager = AchievementManager(
        context = application,
        repository = repository,
        onAchievementUnlocked = { achievementId, title, points ->
            // 可以在這裡處理成就解鎖的 UI 回調
        }
    )

    init {
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch {
            achievementManager.initialize()
        }
    }

    fun getAchievementManager(): AchievementManager = achievementManager

    fun unlockAchievement(achievementId: String) {
        viewModelScope.launch {
            repository.unlockAchievement(achievementId)
        }
    }

    /**
     * 獲取等級進度 (進度百分比, 距離下一等級還需要的積分)
     */
    fun getLevelProgress(totalPoints: Int): Pair<Int, Int> {
        val currentLevel = achievementManager.calculateLevel(totalPoints)
        val nextLevelPoints = achievementManager.getPointsForNextLevel(totalPoints)
        val currentLevelMinPoints = when (currentLevel) {
            1 -> 0
            2 -> 200
            3 -> 500
            4 -> 1000
            5 -> 2000
            else -> 0
        }

        val progress = if (currentLevel == 5) {
            100 // 最高等級
        } else {
            val levelRange = nextLevelPoints - currentLevelMinPoints
            val currentProgress = totalPoints - currentLevelMinPoints
            (currentProgress * 100 / levelRange).coerceIn(0, 100)
        }

        return Pair(progress, nextLevelPoints - totalPoints)
    }

    /**
     * 獲取分類別的成就
     */
    fun getAchievementsByCategory(): Map<String, List<Achievement>> {
        val achievements = allAchievements.value ?: emptyList()
        return achievements.groupBy { it.category }
    }

    /**
     * 獲取等級名稱
     */
    fun getLevelName(level: Int): String = achievementManager.getLevelName(level)

    /**
     * 獲取未解鎖成就數量
     */
    fun getLockedAchievementCount(): Int {
        return lockedAchievements.value?.size ?: 0
    }

    /**
     * 獲取已解鎖成就數量
     */
    fun getUnlockedAchievementCount(): Int {
        return unlockedAchievements.value?.size ?: 0
    }
}