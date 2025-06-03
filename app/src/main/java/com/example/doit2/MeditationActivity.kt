package com.example.doit2

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doit2.databinding.ActivityMeditationBinding

class MeditationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMeditationBinding
    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private var timeLeftInMillis = 300000L // 預設 5 分鐘
    private var selectedTimeInMillis = 300000L // 選擇的時間

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeditationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
        updateTimerDisplay()
        updateButtons()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "靜心倒數"
    }

    private fun setupClickListeners() {
        // 時間選擇按鈕
        binding.btn1Min.setOnClickListener { selectTime(60000L) }
        binding.btn3Min.setOnClickListener { selectTime(180000L) }
        binding.btn5Min.setOnClickListener { selectTime(300000L) }
        binding.btn10Min.setOnClickListener { selectTime(600000L) }

        // 控制按鈕
        binding.btnPlay.setOnClickListener { startTimer() }
        binding.btnPause.setOnClickListener { pauseTimer() }
        binding.btnReset.setOnClickListener { resetTimer() }
    }

    private fun selectTime(timeInMillis: Long) {
        if (!isTimerRunning) {
            selectedTimeInMillis = timeInMillis
            timeLeftInMillis = timeInMillis
            updateTimerDisplay()
            updateTimeButtons()
        }
    }

    private fun startTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true

            countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeLeftInMillis = millisUntilFinished
                    updateTimerDisplay()
                    updateProgress()
                }

                override fun onFinish() {
                    isTimerRunning = false
                    timeLeftInMillis = 0
                    updateTimerDisplay()
                    updateButtons()
                    onTimerComplete()
                }
            }.start()

            updateButtons()
        }
    }

    private fun pauseTimer() {
        if (isTimerRunning) {
            countDownTimer?.cancel()
            isTimerRunning = false
            updateButtons()
        }
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        timeLeftInMillis = selectedTimeInMillis
        updateTimerDisplay()
        updateButtons()
        updateProgress()
    }

    private fun updateTimerDisplay() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun updateProgress() {
        val progress = ((selectedTimeInMillis - timeLeftInMillis).toFloat() / selectedTimeInMillis * 100).toInt()
        binding.progressBar.progress = progress
    }

    private fun updateButtons() {
        when {
            isTimerRunning -> {
                binding.btnPlay.isEnabled = false
                binding.btnPause.isEnabled = true
                binding.btnReset.isEnabled = true
            }
            timeLeftInMillis > 0 -> {
                binding.btnPlay.isEnabled = true
                binding.btnPause.isEnabled = false
                binding.btnReset.isEnabled = true
            }
            else -> {
                binding.btnPlay.isEnabled = false
                binding.btnPause.isEnabled = false
                binding.btnReset.isEnabled = true
            }
        }
    }

    private fun updateTimeButtons() {
        // 重置所有按鈕為未選中狀態（白色背景，紫色文字）
        val buttons = listOf(binding.btn1Min, binding.btn3Min, binding.btn5Min, binding.btn10Min)
        buttons.forEach { button ->
            button.backgroundTintList = getColorStateList(R.color.white)
            button.setTextColor(getColor(R.color.primary_color))
        }

        // 設定選中的按鈕（紫色背景，白色文字）
        val selectedButton = when (selectedTimeInMillis) {
            60000L -> binding.btn1Min
            180000L -> binding.btn3Min
            300000L -> binding.btn5Min
            600000L -> binding.btn10Min
            else -> null
        }

        selectedButton?.let { button ->
            button.backgroundTintList = getColorStateList(R.color.primary_color)
            button.setTextColor(getColor(R.color.white))
        }
    }

    private fun onTimerComplete() {
        Toast.makeText(this, "🧘‍♀️ 靜心完成！做得很好！", Toast.LENGTH_LONG).show()
        binding.progressBar.progress = 100
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}