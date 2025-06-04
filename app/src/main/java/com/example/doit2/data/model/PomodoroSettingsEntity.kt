package com.example.doit2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pomodoro_settings")
data class PomodoroSettings(
    @PrimaryKey
    val id: Int = 1,                       // 固定ID，只有一筆設定記錄
    val focusDuration: Int = 25,           // 專注時間（分鐘）
    val shortBreakDuration: Int = 5,       // 短休息時間（分鐘）
    val longBreakDuration: Int = 15,       // 長休息時間（分鐘）
    val sessionsUntilLongBreak: Int = 4,   // 幾個專注週期後長休息
    val enableNotifications: Boolean = true,  // 是否啟用通知
    val enableVibration: Boolean = true,       // 是否啟用震動
    val autoStartBreak: Boolean = false,       // 是否自動開始休息
    val autoStartFocus: Boolean = false        // 是否自動開始專注
) {
    /**
     * 預設的時間選項
     */
    companion object {
        val FOCUS_DURATION_OPTIONS = listOf(15, 25, 30, 45, 60)      // 專注時間選項
        val SHORT_BREAK_OPTIONS = listOf(5, 10, 15)                  // 短休息選項
        val LONG_BREAK_OPTIONS = listOf(15, 20, 30)                  // 長休息選項
    }
}