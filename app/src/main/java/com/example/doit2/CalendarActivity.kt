package com.example.doit2

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.doit2.databinding.ActivityCalendarBinding
import com.example.doit2.ui.viewmodel.CalendarViewModel
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding
    private val calendarViewModel: CalendarViewModel by viewModels()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("CalendarActivity", "Activity 創建")

        setupToolbar()
        setupClickListeners()
        setupObservers()
        setupCalendarView()

        // 立即更新數據
        calendarViewModel.updateTodayTaskStats()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "📅 日曆追蹤"
    }

    private fun setupClickListeners() {
        Log.d("CalendarActivity", "設置點擊事件")

        // 月份導航按鈕
        binding.btnPreviousMonth.setOnClickListener {
            Log.d("CalendarActivity", "點擊上個月")
            calendarViewModel.goToPreviousMonth()
        }

        binding.btnNextMonth.setOnClickListener {
            Log.d("CalendarActivity", "點擊下個月")
            calendarViewModel.goToNextMonth()
        }

        binding.btnToday.setOnClickListener {
            Log.d("CalendarActivity", "點擊回到今天")
            calendarViewModel.goToCurrentMonth()
        }

        // 日曆日期選擇
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            Log.d("CalendarActivity", "點擊日期: $selectedDate")

            calendarViewModel.selectDate(selectedDate)
        }
    }

    private fun setupObservers() {
        Log.d("CalendarActivity", "設置觀察者")

        // 觀察當前月份
        calendarViewModel.currentMonth.observe(this) { (year, month) ->
            Log.d("CalendarActivity", "月份更新: $year-$month")
            updateMonthDisplay(year, month)
        }

        // 觀察月度統計
        calendarViewModel.monthlyStats.observe(this) { stats ->
            Log.d("CalendarActivity", "統計更新: $stats")
            updateMonthlyStats(stats)
        }

        // 觀察連續天數
        calendarViewModel.currentStreak.observe(this) { streak ->
            Log.d("CalendarActivity", "連續天數: $streak")
            binding.tvCurrentStreak.text = streak.toString()
        }

        // 觀察選中日期詳情
        calendarViewModel.selectedDateRecord.observe(this) { record ->
            Log.d("CalendarActivity", "選中日期記錄: $record")
            updateSelectedDateDetails(record)
        }
    }

    private fun setupCalendarView() {
        // 設置日曆初始狀態
        val calendar = Calendar.getInstance()
        binding.calendarView.date = calendar.timeInMillis
        Log.d("CalendarActivity", "日曆初始化完成")
    }

    private fun updateMonthDisplay(year: Int, month: Int) {
        binding.tvCurrentMonth.text = calendarViewModel.getMonthDisplayName(year, month)

        // 更新 CalendarView 到指定月份
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1) // Calendar.MONTH 從0開始
        binding.calendarView.date = calendar.timeInMillis

        Log.d("CalendarActivity", "月份顯示更新: ${year}年${month}月")
    }

    private fun updateMonthlyStats(stats: com.example.doit2.data.repository.MonthlyStats) {
        with(binding) {
            tvActiveDays.text = stats.activeDays.toString()
            tvPerfectDays.text = stats.perfectDays.toString()
            tvCompletionRate.text = "${stats.completionRate}%"
        }
        Log.d("CalendarActivity", "統計顯示更新: 活躍=${stats.activeDays}, 完美=${stats.perfectDays}, 完成率=${stats.completionRate}%")
    }

    private fun updateSelectedDateDetails(record: com.example.doit2.data.model.DailyRecord?) {
        // 無論有沒有記錄都顯示詳情卡片
        binding.cardSelectedDate.visibility = View.VISIBLE

        if (record != null && record.totalTasks > 0) {
            // 有任務的情況
            with(binding) {
                // 格式化日期顯示
                val displayDate = formatDateForDisplay(record.date)
                tvSelectedDateTitle.text = displayDate

                // 顯示統計數據
                tvDateTotalTasks.text = record.totalTasks.toString()
                tvDateCompletedTasks.text = record.completedTasks.toString()
                tvDateCompletionRate.text = "${record.completionRate}%"

                Log.d("CalendarActivity", "日期詳情顯示: ${record.date} - ${record.completedTasks}/${record.totalTasks}")
            }
        } else {
            // 無任務的情況
            with(binding) {
                // 格式化日期顯示
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val selectedDate = record?.date ?: today
                val displayDate = formatDateForDisplay(selectedDate)
                tvSelectedDateTitle.text = displayDate

                // 顯示無任務狀態
                tvDateTotalTasks.text = "0"
                tvDateCompletedTasks.text = "0"
                tvDateCompletionRate.text = "無任務"

                Log.d("CalendarActivity", "顯示無任務狀態: $selectedDate")
            }
        }
    }

    private fun formatDateForDisplay(dateString: String): String {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
            val displayFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
            displayFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onResume() {
        super.onResume()
        Log.d("CalendarActivity", "Activity onResume - 重新載入數據")

        // 重新載入數據
        calendarViewModel.updateTodayTaskStats()
    }
}