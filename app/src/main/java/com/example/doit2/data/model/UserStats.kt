package com.example.doit2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.doit2.data.database.Converters
import java.util.Date

@Entity(tableName = "user_stats")
@TypeConverters(Converters::class)
data class UserStats(
    @PrimaryKey
    val id: Int = 1,                      // 固定ID，只有一筆記錄
    val totalPoints: Int = 0,             // 總積分
    val currentLevel: Int = 1,            // 當前等級 (1-5)
    val completedTasks: Int = 0,          // 累計完成任務數
    val currentStreak: Int = 0,           // 當前連續天數
    val longestStreak: Int = 0,           // 最長連續天數
    val lastActiveDate: Date = Date(),    // 最後活躍日期
    val meditationMinutes: Int = 0,       // 累計靜心分鐘數
    val musicUsageCount: Int = 0,         // 音樂使用次數
    val pomodoroSessions: Int = 0,        // 番茄鐘完成次數
    val modulesUnlocked: String = "",     // 已解鎖的模組 (逗號分隔字符串)
    val todayCompletedTasks: Int = 0,     // 今日完成任務數
    val todayActiveDate: Date = Date()    // 今日記錄日期
)