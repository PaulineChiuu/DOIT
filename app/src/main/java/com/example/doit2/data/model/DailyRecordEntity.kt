package com.example.doit2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.doit2.data.database.Converters
import java.util.Date

@Entity(tableName = "daily_records")
@TypeConverters(Converters::class)
data class DailyRecord(
    @PrimaryKey
    val date: String,                    // 日期格式: "yyyy-MM-dd"
    val totalTasks: Int = 0,             // 當日總任務數
    val completedTasks: Int = 0,         // 當日完成任務數
    val appUsed: Boolean = false,        // 當日是否使用過App
    val firstTaskTime: Date? = null,     // 第一個任務完成時間
    val lastTaskTime: Date? = null,      // 最後一個任務完成時間
    val meditationMinutes: Int = 0,      // 當日靜心分鐘數
    val musicUsed: Boolean = false,      // 當日是否使用音樂
    val pomodoroSessions: Int = 0,       // 當日番茄鐘次數
    val recordedAt: Date = Date()        // 記錄時間
) {
    /**
     * 計算當日完成率 (0-100)
     */
    val completionRate: Int
        get() = if (totalTasks > 0) (completedTasks * 100 / totalTasks) else 0

    /**
     * 獲取當日狀態
     */
    val dayStatus: DayStatus
        get() = when {
            totalTasks == 0 -> DayStatus.NO_TASKS        // 無任務
            completedTasks == totalTasks -> DayStatus.PERFECT    // 全部完成
            completedTasks > 0 -> DayStatus.PARTIAL      // 部分完成
            else -> DayStatus.INCOMPLETE                 // 未完成
        }
}

/**
 * 日期狀態枚舉
 */
enum class DayStatus {
    NO_TASKS,    // 無任務（灰色）
    PERFECT,     // 完美完成（綠色）
    PARTIAL,     // 部分完成（黃色）
    INCOMPLETE   // 未完成（紅色）
}