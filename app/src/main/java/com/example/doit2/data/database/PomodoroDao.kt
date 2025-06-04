package com.example.doit2.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.doit2.data.model.PomodoroSession
import com.example.doit2.data.model.PomodoroSettings
import com.example.doit2.data.model.PomodoroType

@Dao
interface PomodoroDao {

    // === PomodoroSession 操作 ===

    @Query("SELECT * FROM pomodoro_sessions ORDER BY startTime DESC")
    fun getAllSessions(): LiveData<List<PomodoroSession>>

    @Query("SELECT * FROM pomodoro_sessions WHERE sessionType = :type ORDER BY startTime DESC")
    fun getSessionsByType(type: PomodoroType): LiveData<List<PomodoroSession>>

    @Query("SELECT * FROM pomodoro_sessions WHERE date(startTime/1000, 'unixepoch', 'localtime') = :date ORDER BY startTime DESC")
    suspend fun getSessionsByDate(date: String): List<PomodoroSession>

    @Query("SELECT * FROM pomodoro_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): PomodoroSession?

    @Insert
    suspend fun insertSession(session: PomodoroSession): Long

    @Update
    suspend fun updateSession(session: PomodoroSession)

    @Delete
    suspend fun deleteSession(session: PomodoroSession)

    @Query("DELETE FROM pomodoro_sessions")
    suspend fun deleteAllSessions()

    // === PomodoroSettings 操作 ===

    @Query("SELECT * FROM pomodoro_settings WHERE id = 1")
    fun getSettings(): LiveData<PomodoroSettings?>

    @Query("SELECT * FROM pomodoro_settings WHERE id = 1")
    suspend fun getSettingsSync(): PomodoroSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: PomodoroSettings)

    @Update
    suspend fun updateSettings(settings: PomodoroSettings)

    // === 統計查詢 ===

    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE sessionType = 'FOCUS' AND isCompleted = 1")
    suspend fun getCompletedFocusSessionsCount(): Int

    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE sessionType = 'FOCUS' AND isCompleted = 1 AND date(startTime/1000, 'unixepoch', 'localtime') = :date")
    suspend fun getTodayCompletedFocusCount(date: String): Int

    @Query("SELECT SUM(actualDuration) FROM pomodoro_sessions WHERE sessionType = 'FOCUS' AND isCompleted = 1")
    suspend fun getTotalFocusMinutes(): Int?

    @Query("SELECT SUM(actualDuration) FROM pomodoro_sessions WHERE sessionType = 'FOCUS' AND isCompleted = 1 AND date(startTime/1000, 'unixepoch', 'localtime') = :date")
    suspend fun getTodayFocusMinutes(date: String): Int?

    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE sessionType = 'FOCUS' AND isCompleted = 1 AND date(startTime/1000, 'unixepoch', 'localtime') BETWEEN :startDate AND :endDate")
    suspend fun getFocusSessionsInDateRange(startDate: String, endDate: String): Int

    // === 最近記錄 ===

    @Query("SELECT * FROM pomodoro_sessions WHERE sessionType = 'FOCUS' ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecentFocusSessions(limit: Int): List<PomodoroSession>

    @Query("SELECT * FROM pomodoro_sessions ORDER BY startTime DESC LIMIT 1")
    suspend fun getLastSession(): PomodoroSession?
}