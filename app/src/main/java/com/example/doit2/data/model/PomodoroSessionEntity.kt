package com.example.doit2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.doit2.data.database.Converters
import java.util.Date

@Entity(tableName = "pomodoro_sessions")
@TypeConverters(Converters::class)
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionType: PomodoroType,          // FOCUS 或 BREAK
    val plannedDuration: Int,               // 計劃時長（分鐘）
    val actualDuration: Int,                // 實際時長（分鐘）
    val isCompleted: Boolean = false,       // 是否完成
    val startTime: Date = Date(),           // 開始時間
    val endTime: Date? = null,              // 結束時間
    val notes: String = "",                 // 備註（可選）
    val taskId: Long? = null,               // 關聯的任務ID（可選）
    val createdAt: Date = Date()
)

/**
 * 番茄鐘類型
 */
enum class PomodoroType {
    FOCUS,    // 專注時間
    BREAK     // 休息時間
}

/**
 * 番茄鐘狀態
 */
enum class PomodoroState {
    IDLE,     // 閒置
    RUNNING,  // 運行中
    PAUSED,   // 暫停
    FINISHED  // 完成
}