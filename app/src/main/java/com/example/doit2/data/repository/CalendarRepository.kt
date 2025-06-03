package com.example.doit2.data.repository

import androidx.lifecycle.LiveData
import com.example.doit2.data.database.DailyRecordDao
import com.example.doit2.data.database.TaskDao
import com.example.doit2.data.model.DailyRecord
import com.example.doit2.data.model.Task
import java.text.SimpleDateFormat
import java.util.*

class CalendarRepository(
    private val dailyRecordDao: DailyRecordDao,
    private val taskDao: TaskDao
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // === DailyRecord 基本操作 ===

    suspend fun getRecordByDate(date: String): DailyRecord? = dailyRecordDao.getRecordByDate(date)

    fun getRecordByDateLive(date: String): LiveData<DailyRecord?> = dailyRecordDao.getRecordByDateLive(date)

    suspend fun getRecordsByMonth(year: Int, month: Int): List<DailyRecord> {
        val monthPattern = String.format("%04d-%02d%%", year, month)
        return dailyRecordDao.getRecordsByMonth(monthPattern)
    }

    fun getRecordsByMonthLive(year: Int, month: Int): LiveData<List<DailyRecord>> {
        val monthPattern = String.format("%04d-%02d%%", year, month)
        return dailyRecordDao.getRecordsByMonthLive(monthPattern)
    }

    suspend fun insertOrUpdateRecord(record: DailyRecord) = dailyRecordDao.insertRecord(record)

    // === 每日記錄更新 ===

    /**
     * 更新今日任務統計
     */
    suspend fun updateTodayTaskStats() {
        val today = dateFormat.format(Date())

        android.util.Log.d("CalendarRepository", "開始更新今日統計: $today")

        try {
            // 調試：檢查最近的任務
            val recentTasks = taskDao.getRecentTasksWithDate()
            android.util.Log.d("CalendarRepository", "最近任務:")
            recentTasks.forEach { task ->
                android.util.Log.d("CalendarRepository", "  - ${task.title} (${task.dateStr}) 完成:${task.isCompleted}")
            }

            // 獲取今日的任務數量
            val todayTasksCount = taskDao.getTasksCountByDate(today)
            val todayCompletedCount = taskDao.getCompletedTasksCountByDate(today)

            android.util.Log.d("CalendarRepository", "查詢結果 - 今日任務: $todayCompletedCount/$todayTasksCount")

            // 如果沒有找到任務，嘗試獲取今日具體任務列表
            if (todayTasksCount == 0) {
                val todayTasks = taskDao.getTasksByDate(today)
                android.util.Log.d("CalendarRepository", "今日具體任務數: ${todayTasks.size}")
                todayTasks.forEach { task ->
                    android.util.Log.d("CalendarRepository", "  - ${task.title} 完成:${task.isCompleted}")
                }
            }

            val existingRecord = getRecordByDate(today)
            val updatedRecord = if (existingRecord != null) {
                existingRecord.copy(
                    totalTasks = todayTasksCount,
                    completedTasks = todayCompletedCount,
                    appUsed = true,
                    recordedAt = Date()
                )
            } else {
                DailyRecord(
                    date = today,
                    totalTasks = todayTasksCount,
                    completedTasks = todayCompletedCount,
                    appUsed = true
                )
            }

            insertOrUpdateRecord(updatedRecord)
            android.util.Log.d("CalendarRepository", "記錄已更新: 總任務=${updatedRecord.totalTasks}, 完成=${updatedRecord.completedTasks}")

        } catch (e: Exception) {
            android.util.Log.e("CalendarRepository", "更新統計失敗", e)
        }
    }

    /**
     * 記錄任務完成
     */
    suspend fun recordTaskCompletion() {
        val today = dateFormat.format(Date())
        val currentTime = Date()

        val existingRecord = getRecordByDate(today) ?: DailyRecord(
            date = today,
            appUsed = true
        )

        val updatedRecord = existingRecord.copy(
            completedTasks = existingRecord.completedTasks + 1,
            firstTaskTime = existingRecord.firstTaskTime ?: currentTime,
            lastTaskTime = currentTime,
            recordedAt = Date()
        )

        insertOrUpdateRecord(updatedRecord)
    }

    /**
     * 記錄靜心使用
     */
    suspend fun recordMeditationUsage(minutes: Int) {
        val today = dateFormat.format(Date())
        val existingRecord = getRecordByDate(today) ?: DailyRecord(
            date = today,
            appUsed = true
        )

        val updatedRecord = existingRecord.copy(
            meditationMinutes = existingRecord.meditationMinutes + minutes,
            recordedAt = Date()
        )

        insertOrUpdateRecord(updatedRecord)
    }

    /**
     * 記錄音樂使用
     */
    suspend fun recordMusicUsage() {
        val today = dateFormat.format(Date())
        val existingRecord = getRecordByDate(today) ?: DailyRecord(
            date = today,
            appUsed = true
        )

        val updatedRecord = existingRecord.copy(
            musicUsed = true,
            recordedAt = Date()
        )

        insertOrUpdateRecord(updatedRecord)
    }

    /**
     * 記錄番茄鐘使用
     */
    suspend fun recordPomodoroSession() {
        val today = dateFormat.format(Date())
        val existingRecord = getRecordByDate(today) ?: DailyRecord(
            date = today,
            appUsed = true
        )

        val updatedRecord = existingRecord.copy(
            pomodoroSessions = existingRecord.pomodoroSessions + 1,
            recordedAt = Date()
        )

        insertOrUpdateRecord(updatedRecord)
    }

    // === 統計計算 ===

    /**
     * 計算當前連續天數
     */
    suspend fun getCurrentStreak(): Int {
        val records = dailyRecordDao.getActiveDaysOrderByDate()
        if (records.isEmpty()) return 0

        val today = dateFormat.format(Date())
        var streak = 0
        val calendar = Calendar.getInstance()

        // 從今天開始往前計算
        for (i in 0 until records.size) {
            val expectedDate = dateFormat.format(calendar.time)
            val record = records.find { it.date == expectedDate }

            if (record != null && record.completedTasks > 0) {
                streak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        return streak
    }

    /**
     * 獲取月度統計
     */
    suspend fun getMonthlyStats(year: Int, month: Int): MonthlyStats {
        val records = getRecordsByMonth(year, month)

        val totalDays = records.size
        val activeDays = records.count { it.completedTasks > 0 }
        val perfectDays = records.count { it.totalTasks > 0 && it.completedTasks == it.totalTasks }
        val totalTasks = records.sumOf { it.totalTasks }
        val completedTasks = records.sumOf { it.completedTasks }
        val completionRate = if (totalTasks > 0) (completedTasks * 100 / totalTasks) else 0

        return MonthlyStats(
            year = year,
            month = month,
            totalDays = totalDays,
            activeDays = activeDays,
            perfectDays = perfectDays,
            totalTasks = totalTasks,
            completedTasks = completedTasks,
            completionRate = completionRate
        )
    }

    /**
     * 初始化歷史數據
     */
    suspend fun initializeHistoricalData() {
        // 暫時簡化實作，只記錄當天的使用
        val today = dateFormat.format(Date())
        val existingRecord = getRecordByDate(today)

        if (existingRecord == null) {
            val record = DailyRecord(
                date = today,
                totalTasks = 0,
                completedTasks = 0,
                appUsed = true
            )
            insertOrUpdateRecord(record)
        }
    }
}

/**
 * 月度統計數據類
 */
data class MonthlyStats(
    val year: Int,
    val month: Int,
    val totalDays: Int,           // 有記錄的天數
    val activeDays: Int,          // 有完成任務的天數
    val perfectDays: Int,         // 100%完成的天數
    val totalTasks: Int,          // 總任務數
    val completedTasks: Int,      // 完成任務數
    val completionRate: Int       // 完成率百分比
)