package com.example.doit2.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.doit2.data.model.Achievement

@Dao
interface AchievementDao {

    @Query("SELECT * FROM achievements ORDER BY category, points")
    fun getAllAchievements(): LiveData<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE isUnlocked = 1 ORDER BY unlockedDate DESC")
    fun getUnlockedAchievements(): LiveData<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE isUnlocked = 0 ORDER BY category, points")
    fun getLockedAchievements(): LiveData<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE id = :achievementId")
    suspend fun getAchievementById(achievementId: String): Achievement?

    @Query("SELECT * FROM achievements WHERE category = :category")
    suspend fun getAchievementsByCategory(category: String): List<Achievement>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<Achievement>)

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    @Query("DELETE FROM achievements")
    suspend fun deleteAllAchievements()

    @Query("SELECT COUNT(*) FROM achievements WHERE isUnlocked = 1")
    suspend fun getUnlockedCount(): Int

    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getTotalCount(): Int
}