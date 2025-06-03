package com.example.doit2.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.doit2.data.model.UserStats

@Dao
interface UserStatsDao {

    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStats(): LiveData<UserStats?>

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getUserStatsSync(): UserStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(userStats: UserStats)

    @Update
    suspend fun updateUserStats(userStats: UserStats)

    @Query("UPDATE user_stats SET totalPoints = totalPoints + :points WHERE id = 1")
    suspend fun addPoints(points: Int)

    @Query("UPDATE user_stats SET completedTasks = completedTasks + 1 WHERE id = 1")
    suspend fun incrementCompletedTasks()

    @Query("UPDATE user_stats SET todayCompletedTasks = todayCompletedTasks + 1 WHERE id = 1")
    suspend fun incrementTodayCompletedTasks()

    @Query("UPDATE user_stats SET meditationMinutes = meditationMinutes + :minutes WHERE id = 1")
    suspend fun addMeditationMinutes(minutes: Int)

    @Query("UPDATE user_stats SET musicUsageCount = musicUsageCount + 1 WHERE id = 1")
    suspend fun incrementMusicUsage()

    @Query("UPDATE user_stats SET pomodoroSessions = pomodoroSessions + 1 WHERE id = 1")
    suspend fun incrementPomodoroSessions()

    @Query("DELETE FROM user_stats")
    suspend fun deleteAllStats()
}