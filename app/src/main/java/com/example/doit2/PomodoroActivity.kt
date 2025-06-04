package com.example.doit2

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doit2.databinding.ActivityPomodoroBinding

class PomodoroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPomodoroBinding
    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private var timeLeftInMillis = 1500000L // 預設 25 分鐘
    private var selectedTimeInMillis = 1500000L // 選擇的時間
    private var isBreakMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("PomodoroActivity", "=== 簡化版 Activity onCreate 開始 ===")

        binding = ActivityPomodoroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
        updateTimerDisplay()
        updateButtons()
        hideBreakCard()

        android.util.Log.d("PomodoroActivity", "=== 簡化版 Activity onCreate 完成 ===")
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "🍅 番茄鐘"
    }

    private fun setupClickListeners() {
        android.util.Log.d("PomodoroActivity", "設定點擊監聽器")

        // 時間選擇按鈕
        binding.btn15Min.setOnClickListener { selectTime(15 * 60 * 1000L) }
        binding.btn25Min.setOnClickListener { selectTime(25 * 60 * 1000L) }
        binding.btn30Min.setOnClickListener { selectTime(30 * 60 * 1000L) }
        binding.btn45Min.setOnClickListener { selectTime(45 * 60 * 1000L) }

        // 控制按鈕
        binding.fabPlayPause.setOnClickListener {
            android.util.Log.d("PomodoroActivity", "=== 點擊播放/暫停按鈕 ===")
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        binding.fabStop.setOnClickListener {
            android.util.Log.d("PomodoroActivity", "=== 點擊停止按鈕 ===")
            resetTimer()
        }

        // 開始休息按鈕
        binding.btnStartBreak.setOnClickListener {
            android.util.Log.d("PomodoroActivity", "=== 點擊開始休息按鈕 ===")
            startBreakSession()
        }
    }

    private fun selectTime(timeInMillis: Long) {
        if (!isTimerRunning) {
            selectedTimeInMillis = timeInMillis
            timeLeftInMillis = timeInMillis
            updateTimerDisplay()
            updateTimeButtons()
            android.util.Log.d("PomodoroActivity", "選擇時間: ${timeInMillis / 60000} 分鐘")
        }
    }

    private fun startTimer() {
        android.util.Log.d("PomodoroActivity", "=== 開始計時器 ===")
        android.util.Log.d("PomodoroActivity", "時間: ${timeLeftInMillis / 60000} 分鐘")

        if (!isTimerRunning) {
            isTimerRunning = true

            countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeLeftInMillis = millisUntilFinished
                    updateTimerDisplay()
                    updateProgress()

                    // 每10秒輸出一次日誌
                    if ((millisUntilFinished / 1000) % 10 == 0L) {
                        android.util.Log.d("PomodoroActivity", "⏰ 倒數中: ${millisUntilFinished / 1000} 秒")
                    }
                }

                override fun onFinish() {
                    android.util.Log.d("PomodoroActivity", "🎉 計時完成！")
                    isTimerRunning = false
                    timeLeftInMillis = 0
                    updateTimerDisplay()
                    updateButtons()
                    onTimerComplete()
                }
            }.start()

            updateButtons()
            android.util.Log.d("PomodoroActivity", "✅ 計時器啟動成功")
        }
    }

    private fun pauseTimer() {
        android.util.Log.d("PomodoroActivity", "⏸️ 暫停計時器")
        if (isTimerRunning) {
            countDownTimer?.cancel()
            isTimerRunning = false
            updateButtons()
        }
    }

    private fun resetTimer() {
        android.util.Log.d("PomodoroActivity", "🔄 重置計時器")
        countDownTimer?.cancel()
        isTimerRunning = false
        timeLeftInMillis = selectedTimeInMillis
        isBreakMode = false
        updateTimerDisplay()
        updateButtons()
        updateProgress()
        hideBreakCard()
        updateModeDisplay()
    }

    private fun startBreakSession() {
        android.util.Log.d("PomodoroActivity", "☕ 開始休息時間")
        isBreakMode = true
        selectedTimeInMillis = 5 * 60 * 1000L // 5分鐘休息
        timeLeftInMillis = selectedTimeInMillis
        updateTimerDisplay()
        updateModeDisplay()
        hideBreakCard()
        startTimer()
    }

    private fun updateTimerDisplay() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeText = String.format("%02d:%02d", minutes, seconds)
        binding.tvTimerDisplay.text = timeText

        // 更新狀態文字
        binding.tvTimerStatus.text = when {
            isTimerRunning && isBreakMode -> "休息中..."
            isTimerRunning -> "專注中..."
            timeLeftInMillis == 0L -> "完成！"
            else -> "準備開始"
        }
    }

    private fun updateProgress() {
        val progress = ((selectedTimeInMillis - timeLeftInMillis).toFloat() / selectedTimeInMillis * 100).toInt()
        binding.progressTimer.progress = progress
    }

    private fun updateButtons() {
        when {
            isTimerRunning -> {
                binding.fabPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                enableDurationButtons(false)
            }
            timeLeftInMillis > 0 -> {
                binding.fabPlayPause.setImageResource(android.R.drawable.ic_media_play)
                enableDurationButtons(true)
            }
            else -> {
                binding.fabPlayPause.setImageResource(android.R.drawable.ic_media_play)
                enableDurationButtons(true)
            }
        }
    }

    private fun updateTimeButtons() {
        // 重置所有按鈕樣式
        val buttons = listOf(binding.btn15Min, binding.btn25Min, binding.btn30Min, binding.btn45Min)
        buttons.forEach { button ->
            try {
                button.backgroundTintList = getColorStateList(R.color.white)
                button.setTextColor(getColor(R.color.primary_color))
            } catch (e: Exception) {
                android.util.Log.e("PomodoroActivity", "更新按鈕樣式失敗", e)
            }
        }

        // 高亮選中的按鈕
        val selectedButton = when (selectedTimeInMillis) {
            15 * 60 * 1000L -> binding.btn15Min
            25 * 60 * 1000L -> binding.btn25Min
            30 * 60 * 1000L -> binding.btn30Min
            45 * 60 * 1000L -> binding.btn45Min
            else -> null
        }

        selectedButton?.let { button ->
            try {
                button.backgroundTintList = getColorStateList(R.color.primary_color)
                button.setTextColor(getColor(R.color.white))
            } catch (e: Exception) {
                android.util.Log.e("PomodoroActivity", "設定選中按鈕樣式失敗", e)
            }
        }
    }

    private fun updateModeDisplay() {
        val modeText = if (isBreakMode) "休息模式" else "專注模式"
        binding.tvCurrentMode.text = modeText
    }

    private fun enableDurationButtons(enabled: Boolean) {
        binding.btn15Min.isEnabled = enabled
        binding.btn25Min.isEnabled = enabled
        binding.btn30Min.isEnabled = enabled
        binding.btn45Min.isEnabled = enabled
    }

    private fun showBreakCard() {
        binding.cardNextBreak.visibility = View.VISIBLE
        binding.btnStartBreak.text = "開始休息(5分)"
    }

    private fun hideBreakCard() {
        binding.cardNextBreak.visibility = View.GONE
    }

    private fun onTimerComplete() {
        if (isBreakMode) {
            Toast.makeText(this, "☕ 休息完成！準備好繼續專注了嗎？", Toast.LENGTH_LONG).show()
            isBreakMode = false
            // 重置為預設專注時間
            selectedTimeInMillis = 25 * 60 * 1000L
            timeLeftInMillis = selectedTimeInMillis
            updateModeDisplay()
        } else {
            Toast.makeText(this, "🍅 專注完成！是時候休息一下了", Toast.LENGTH_LONG).show()
            showBreakCard()
        }

        updateProgress()
        updateTimerDisplay()

        // 更新統計（簡化版）
        binding.tvTodaySessions.text = "1" // 簡化統計
        binding.tvTodayMinutes.text = "${selectedTimeInMillis / 60000}"
        binding.tvTotalSessions.text = "1"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.d("PomodoroActivity", "Activity 銷毀")
        countDownTimer?.cancel()
    }
}