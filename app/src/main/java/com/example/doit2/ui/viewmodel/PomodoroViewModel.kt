package com.example.doit2.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 簡化版番茄鐘 ViewModel
 * 配合 CountDownTimer 使用，不依賴複雜的 Service 和廣播
 */
class PomodoroViewModel(application: Application) : AndroidViewModel(application) {

    // SharedPreferences 用於簡單的數據存儲
    private val prefs = application.getSharedPreferences("pomodoro_stats", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // 基本統計數據
    private val _todayStats = MutableLiveData<PomodoroStats>()
    val todayStats: LiveData<PomodoroStats> = _todayStats

    // 設定數據
    private val _settings = MutableLiveData<PomodoroSettings>()
    val settings: LiveData<PomodoroSettings> = _settings

    init {
        android.util.Log.d("PomodoroViewModel", "✅ 簡化版 PomodoroViewModel 初始化")
        loadSettings()
        loadTodayStats()
    }

    /**
     * 載入設定
     */
    private fun loadSettings() {
        val settings = PomodoroSettings(
            focusDuration = prefs.getInt("focus_duration", 25),
            shortBreakDuration = prefs.getInt("short_break_duration", 5),
            longBreakDuration = prefs.getInt("long_break_duration", 15),
            sessionsUntilLongBreak = prefs.getInt("sessions_until_long_break", 4)
        )
        _settings.value = settings
        android.util.Log.d("PomodoroViewModel", "載入設定: $settings")
    }

    /**
     * 儲存設定
     */
    fun updateSettings(settings: PomodoroSettings) {
        prefs.edit().apply {
            putInt("focus_duration", settings.focusDuration)
            putInt("short_break_duration", settings.shortBreakDuration)
            putInt("long_break_duration", settings.longBreakDuration)
            putInt("sessions_until_long_break", settings.sessionsUntilLongBreak)
            apply()
        }
        _settings.value = settings
        android.util.Log.d("PomodoroViewModel", "更新設定: $settings")
    }

    /**
     * 載入今日統計
     */
    private fun loadTodayStats() {
        val today = dateFormat.format(Date())
        val todayKey = "stats_$today"

        val todayFocusCount = prefs.getInt("${todayKey}_focus_count", 0)
        val todayFocusMinutes = prefs.getInt("${todayKey}_focus_minutes", 0)
        val totalFocusCount = prefs.getInt("total_focus_count", 0)
        val totalFocusMinutes = prefs.getInt("total_focus_minutes", 0)

        val stats = PomodoroStats(
            todayFocusCount = todayFocusCount,
            todayFocusMinutes = todayFocusMinutes,
            totalFocusCount = totalFocusCount,
            totalFocusMinutes = totalFocusMinutes
        )

        _todayStats.value = stats
        android.util.Log.d("PomodoroViewModel", "載入統計: $stats")
    }

    /**
     * 記錄完成的專注會話
     */
    fun recordFocusSession(durationMinutes: Int) {
        android.util.Log.d("PomodoroViewModel", "記錄專注會話: $durationMinutes 分鐘")

        viewModelScope.launch {
            val today = dateFormat.format(Date())
            val todayKey = "stats_$today"

            // 更新今日統計
            val todayFocusCount = prefs.getInt("${todayKey}_focus_count", 0) + 1
            val todayFocusMinutes = prefs.getInt("${todayKey}_focus_minutes", 0) + durationMinutes

            // 更新總統計
            val totalFocusCount = prefs.getInt("total_focus_count", 0) + 1
            val totalFocusMinutes = prefs.getInt("total_focus_minutes", 0) + durationMinutes

            // 儲存到 SharedPreferences
            prefs.edit().apply {
                putInt("${todayKey}_focus_count", todayFocusCount)
                putInt("${todayKey}_focus_minutes", todayFocusMinutes)
                putInt("total_focus_count", totalFocusCount)
                putInt("total_focus_minutes", totalFocusMinutes)

                // 記錄最後完成時間
                putLong("last_session_time", System.currentTimeMillis())
                apply()
            }

            // 更新 LiveData
            val newStats = PomodoroStats(
                todayFocusCount = todayFocusCount,
                todayFocusMinutes = todayFocusMinutes,
                totalFocusCount = totalFocusCount,
                totalFocusMinutes = totalFocusMinutes
            )
            _todayStats.postValue(newStats)

            android.util.Log.d("PomodoroViewModel", "統計已更新: $newStats")
        }
    }

    /**
     * 記錄完成的休息會話
     */
    fun recordBreakSession(durationMinutes: Int) {
        android.util.Log.d("PomodoroViewModel", "記錄休息會話: $durationMinutes 分鐘")

        viewModelScope.launch {
            val today = dateFormat.format(Date())
            val todayKey = "stats_$today"

            val todayBreakCount = prefs.getInt("${todayKey}_break_count", 0) + 1
            val todayBreakMinutes = prefs.getInt("${todayKey}_break_minutes", 0) + durationMinutes

            prefs.edit().apply {
                putInt("${todayKey}_break_count", todayBreakCount)
                putInt("${todayKey}_break_minutes", todayBreakMinutes)
                apply()
            }
        }
    }

    /**
     * 取得建議的休息時間
     */
    fun getRecommendedBreakDuration(): Int {
        val currentStats = _todayStats.value ?: return 5
        val settings = _settings.value ?: return 5

        // 根據今日完成的專注次數決定休息時間
        return if (currentStats.todayFocusCount > 0 &&
            currentStats.todayFocusCount % settings.sessionsUntilLongBreak == 0) {
            settings.longBreakDuration // 長休息
        } else {
            settings.shortBreakDuration // 短休息
        }
    }

    /**
     * 檢查是否應該長休息
     */
    fun shouldShowLongBreak(): Boolean {
        val currentStats = _todayStats.value ?: return false
        val settings = _settings.value ?: return false

        return currentStats.todayFocusCount > 0 &&
                (currentStats.todayFocusCount + 1) % settings.sessionsUntilLongBreak == 0
    }

    /**
     * 取得預設專注時間
     */
    fun getDefaultFocusDuration(): Int {
        return _settings.value?.focusDuration ?: 25
    }

    /**
     * 重置今日統計（測試用）
     */
    fun resetTodayStats() {
        android.util.Log.d("PomodoroViewModel", "重置今日統計")

        val today = dateFormat.format(Date())
        val todayKey = "stats_$today"

        prefs.edit().apply {
            remove("${todayKey}_focus_count")
            remove("${todayKey}_focus_minutes")
            remove("${todayKey}_break_count")
            remove("${todayKey}_break_minutes")
            apply()
        }

        loadTodayStats()
    }

    /**
     * 取得使用歷史摘要
     */
    fun getUsageSummary(): String {
        val stats = _todayStats.value ?: return "尚未開始使用番茄鐘"

        return buildString {
            append("今日：${stats.todayFocusCount} 個番茄，${stats.todayFocusMinutes} 分鐘\n")
            append("總計：${stats.totalFocusCount} 個番茄，${stats.totalFocusMinutes} 分鐘")

            if (stats.totalFocusCount > 0) {
                val avgSession = stats.totalFocusMinutes / stats.totalFocusCount
                append("\n平均每個番茄：$avgSession 分鐘")
            }
        }
    }
}

/**
 * 番茄鐘統計數據
 */
data class PomodoroStats(
    val todayFocusCount: Int = 0,       // 今日專注次數
    val todayFocusMinutes: Int = 0,     // 今日專注分鐘數
    val totalFocusCount: Int = 0,       // 總專注次數
    val totalFocusMinutes: Int = 0      // 總專注分鐘數
)

/**
 * 番茄鐘設定
 */
data class PomodoroSettings(
    val focusDuration: Int = 25,           // 專注時間（分鐘）
    val shortBreakDuration: Int = 5,       // 短休息時間（分鐘）
    val longBreakDuration: Int = 15,       // 長休息時間（分鐘）
    val sessionsUntilLongBreak: Int = 4    // 幾次專注後長休息
)