package com.example.doit2.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.doit2.data.database.AppDatabase
import com.example.doit2.data.model.DailyRecord
import com.example.doit2.data.repository.CalendarRepository
import com.example.doit2.data.repository.MonthlyStats
import kotlinx.coroutines.launch
import java.util.*

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = CalendarRepository(database.dailyRecordDao(), database.taskDao())

    // 當前選擇的月份
    private val _currentMonth = MutableLiveData<Pair<Int, Int>>()
    val currentMonth: LiveData<Pair<Int, Int>> = _currentMonth

    // 當前月份的記錄
    private val _monthlyRecords = MutableLiveData<List<DailyRecord>>()
    val monthlyRecords: LiveData<List<DailyRecord>> = _monthlyRecords

    // 月度統計
    private val _monthlyStats = MutableLiveData<MonthlyStats>()
    val monthlyStats: LiveData<MonthlyStats> = _monthlyStats

    // 選擇的日期記錄
    private val _selectedDateRecord = MutableLiveData<DailyRecord?>()
    val selectedDateRecord: LiveData<DailyRecord?> = _selectedDateRecord

    // 連續天數
    private val _currentStreak = MutableLiveData<Int>()
    val currentStreak: LiveData<Int> = _currentStreak

    init {
        // 初始化為當前月份
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonthValue = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH 是從0開始

        _currentMonth.value = Pair(currentYear, currentMonthValue)
        loadMonthData(currentYear, currentMonthValue)

        // 初始化歷史數據
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch {
            repository.initializeHistoricalData()
            updateCurrentStreak()
        }
    }

    /**
     * 載入指定月份的數據
     */
    fun loadMonthData(year: Int, month: Int) {
        viewModelScope.launch {
            try {
                // 強制更新今日統計
                repository.updateTodayTaskStats()

                // 載入月份數據
                val records = repository.getRecordsByMonth(year, month)
                val stats = repository.getMonthlyStats(year, month)

                _monthlyRecords.value = records
                _monthlyStats.value = stats
                _currentMonth.value = Pair(year, month)

                // Debug 輸出
                android.util.Log.d("CalendarViewModel", "載入月份: $year-$month, 記錄數: ${records.size}")
                android.util.Log.d("CalendarViewModel", "統計: 活躍天數=${stats.activeDays}, 完美天數=${stats.perfectDays}")

            } catch (e: Exception) {
                android.util.Log.e("CalendarViewModel", "載入月份數據失敗", e)
            }
        }
    }

    /**
     * 選擇特定日期
     */
    fun selectDate(dateString: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("CalendarViewModel", "選擇日期: $dateString")

                // 先更新今日統計
                repository.updateTodayTaskStats()

                // 獲取記錄
                val record = repository.getRecordByDate(dateString)
                _selectedDateRecord.value = record

                android.util.Log.d("CalendarViewModel", "日期記錄: $record")

            } catch (e: Exception) {
                android.util.Log.e("CalendarViewModel", "選擇日期失敗", e)
            }
        }
    }

    /**
     * 切換到上個月
     */
    fun goToPreviousMonth() {
        val current = _currentMonth.value ?: return
        val calendar = Calendar.getInstance()
        calendar.set(current.first, current.second - 1, 1) // Calendar.MONTH 從0開始
        calendar.add(Calendar.MONTH, -1)

        val newYear = calendar.get(Calendar.YEAR)
        val newMonth = calendar.get(Calendar.MONTH) + 1

        loadMonthData(newYear, newMonth)
    }

    /**
     * 切換到下個月
     */
    fun goToNextMonth() {
        val current = _currentMonth.value ?: return
        val calendar = Calendar.getInstance()
        calendar.set(current.first, current.second - 1, 1) // Calendar.MONTH 從0開始
        calendar.add(Calendar.MONTH, 1)

        val newYear = calendar.get(Calendar.YEAR)
        val newMonth = calendar.get(Calendar.MONTH) + 1

        loadMonthData(newYear, newMonth)
    }

    /**
     * 回到當前月份
     */
    fun goToCurrentMonth() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonthValue = calendar.get(Calendar.MONTH) + 1

        loadMonthData(currentYear, currentMonthValue)
    }

    /**
     * 更新今日任務統計
     */
    fun updateTodayTaskStats() {
        viewModelScope.launch {
            try {
                android.util.Log.d("CalendarViewModel", "開始更新今日統計")

                repository.updateTodayTaskStats()

                // 重新載入當前月份數據
                val current = _currentMonth.value
                if (current != null) {
                    loadMonthData(current.first, current.second)
                }

                updateCurrentStreak()

                android.util.Log.d("CalendarViewModel", "今日統計更新完成")

            } catch (e: Exception) {
                android.util.Log.e("CalendarViewModel", "更新統計失敗", e)
            }
        }
    }

    /**
     * 記錄任務完成 - 由TaskViewModel調用
     */
    fun recordTaskCompletion() {
        viewModelScope.launch {
            repository.recordTaskCompletion()
            updateTodayTaskStats()
        }
    }

    /**
     * 記錄靜心使用
     */
    fun recordMeditationUsage(minutes: Int) {
        viewModelScope.launch {
            repository.recordMeditationUsage(minutes)
            updateTodayTaskStats()
        }
    }

    /**
     * 記錄音樂使用
     */
    fun recordMusicUsage() {
        viewModelScope.launch {
            repository.recordMusicUsage()
            updateTodayTaskStats()
        }
    }

    /**
     * 記錄番茄鐘使用
     */
    fun recordPomodoroSession() {
        viewModelScope.launch {
            repository.recordPomodoroSession()
            updateTodayTaskStats()
        }
    }

    /**
     * 更新當前連續天數
     */
    private fun updateCurrentStreak() {
        viewModelScope.launch {
            val streak = repository.getCurrentStreak()
            _currentStreak.value = streak
        }
    }

    /**
     * 獲取日期狀態顏色資源ID
     */
    fun getDateStatusColor(record: DailyRecord?): Int {
        return when (record?.dayStatus) {
            com.example.doit2.data.model.DayStatus.PERFECT -> android.R.color.holo_green_light
            com.example.doit2.data.model.DayStatus.PARTIAL -> android.R.color.holo_orange_light
            com.example.doit2.data.model.DayStatus.INCOMPLETE -> android.R.color.holo_red_light
            com.example.doit2.data.model.DayStatus.NO_TASKS -> android.R.color.darker_gray
            null -> android.R.color.transparent
        }
    }

    /**
     * 獲取月份顯示名稱
     */
    fun getMonthDisplayName(year: Int, month: Int): String {
        val monthNames = arrayOf(
            "1月", "2月", "3月", "4月", "5月", "6月",
            "7月", "8月", "9月", "10月", "11月", "12月"
        )
        return "${year}年 ${monthNames[month - 1]}"
    }
}