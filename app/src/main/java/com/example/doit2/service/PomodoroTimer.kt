package com.example.doit2.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import com.example.doit2.MainActivity
import com.example.doit2.R
import com.example.doit2.data.model.PomodoroState
import com.example.doit2.data.model.PomodoroType
import kotlinx.coroutines.*

class PomodoroTimerService : Service() {

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "pomodoro_timer_channel"

        // Actions
        const val ACTION_START_TIMER = "START_TIMER"
        const val ACTION_PAUSE_TIMER = "PAUSE_TIMER"
        const val ACTION_RESUME_TIMER = "RESUME_TIMER"
        const val ACTION_STOP_TIMER = "STOP_TIMER"

        // Extras
        const val EXTRA_DURATION_MINUTES = "duration_minutes"
        const val EXTRA_SESSION_TYPE = "session_type"
        const val EXTRA_SESSION_ID = "session_id"

        // Broadcast Actions
        const val BROADCAST_TIMER_UPDATE = "com.example.doit2.TIMER_UPDATE"
        const val BROADCAST_TIMER_FINISHED = "com.example.doit2.TIMER_FINISHED"
        const val BROADCAST_TIMER_STATE_CHANGED = "com.example.doit2.TIMER_STATE_CHANGED"
    }

    private val binder = PomodoroTimerBinder()
    private var timerJob: Job? = null
    private var serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Timer state
    private var currentState = PomodoroState.IDLE
    private var sessionType = PomodoroType.FOCUS
    private var sessionId: Long = -1
    private var totalDurationMs = 0L
    private var remainingTimeMs = 0L
    private var startTimeMs = 0L

    // 通知更新控制
    private var lastNotificationUpdate = 0L
    private val NOTIFICATION_UPDATE_INTERVAL = 3000L // 3秒更新一次通知，而不是每秒

    inner class PomodoroTimerBinder : Binder() {
        fun getService(): PomodoroTimerService = this@PomodoroTimerService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        android.util.Log.d("PomodoroTimerService", "服務創建")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        android.util.Log.d("PomodoroTimerService", "收到指令: ${intent?.action}")

        when (intent?.action) {
            ACTION_START_TIMER -> {
                val durationMinutes = intent.getIntExtra(EXTRA_DURATION_MINUTES, 25)
                val typeString = intent.getStringExtra(EXTRA_SESSION_TYPE) ?: "FOCUS"
                val sessionId = intent.getLongExtra(EXTRA_SESSION_ID, -1)

                this.sessionType = PomodoroType.valueOf(typeString)
                this.sessionId = sessionId

                android.util.Log.d("PomodoroTimerService", "開始計時: $durationMinutes 分鐘, 類型: $typeString")
                startTimer(durationMinutes)
            }
            ACTION_PAUSE_TIMER -> {
                android.util.Log.d("PomodoroTimerService", "暫停計時")
                pauseTimer()
            }
            ACTION_RESUME_TIMER -> {
                android.util.Log.d("PomodoroTimerService", "恢復計時")
                resumeTimer()
            }
            ACTION_STOP_TIMER -> {
                android.util.Log.d("PomodoroTimerService", "停止計時")
                stopTimer()
            }
        }

        return START_STICKY
    }

    private fun startTimer(durationMinutes: Int) {
        totalDurationMs = durationMinutes * 60 * 1000L
        remainingTimeMs = totalDurationMs
        startTimeMs = System.currentTimeMillis()
        currentState = PomodoroState.RUNNING
        lastNotificationUpdate = 0L // 重置通知更新時間

        android.util.Log.d("PomodoroTimerService", "計時器啟動: 總時間=${totalDurationMs}ms")

        startForeground(NOTIFICATION_ID, createNotification())
        broadcastStateChanged()

        timerJob = serviceScope.launch {
            while (remainingTimeMs > 0 && currentState == PomodoroState.RUNNING) {
                delay(1000) // 每秒更新

                if (currentState == PomodoroState.RUNNING) {
                    remainingTimeMs -= 1000

                    // 每次都發送廣播更新 UI
                    broadcastTimerUpdate()

                    // 但通知只每 3 秒更新一次
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastNotificationUpdate >= NOTIFICATION_UPDATE_INTERVAL) {
                        updateNotification()
                        lastNotificationUpdate = currentTime
                    }
                }
            }

            if (remainingTimeMs <= 0) {
                onTimerFinished()
            }
        }
    }

    private fun pauseTimer() {
        if (currentState == PomodoroState.RUNNING) {
            currentState = PomodoroState.PAUSED
            timerJob?.cancel()
            updateNotification() // 暫停時立即更新通知
            broadcastStateChanged()
        }
    }

    private fun resumeTimer() {
        if (currentState == PomodoroState.PAUSED) {
            currentState = PomodoroState.RUNNING
            lastNotificationUpdate = 0L // 重置通知更新時間
            broadcastStateChanged()

            timerJob = serviceScope.launch {
                while (remainingTimeMs > 0 && currentState == PomodoroState.RUNNING) {
                    delay(1000)

                    if (currentState == PomodoroState.RUNNING) {
                        remainingTimeMs -= 1000

                        // 每次都發送廣播更新 UI
                        broadcastTimerUpdate()

                        // 但通知只每 3 秒更新一次
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastNotificationUpdate >= NOTIFICATION_UPDATE_INTERVAL) {
                            updateNotification()
                            lastNotificationUpdate = currentTime
                        }
                    }
                }

                if (remainingTimeMs <= 0) {
                    onTimerFinished()
                }
            }
        }
    }

    private fun stopTimer() {
        currentState = PomodoroState.IDLE
        timerJob?.cancel()
        stopForeground(true)
        broadcastStateChanged()
        stopSelf()
    }

    private fun onTimerFinished() {
        currentState = PomodoroState.FINISHED

        android.util.Log.d("PomodoroTimerService", "計時完成: 會話ID=$sessionId, 類型=$sessionType")

        // 發送完成廣播
        val intent = Intent(BROADCAST_TIMER_FINISHED).apply {
            putExtra("session_type", sessionType.name)
            putExtra("session_id", sessionId)
            putExtra("actual_minutes", (totalDurationMs - remainingTimeMs) / 60000)
        }
        sendBroadcast(intent)

        // 顯示完成通知
        showCompletionNotification()
        broadcastStateChanged()

        // 延遲停止服務
        serviceScope.launch {
            delay(3000)
            stopTimer()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "番茄鐘計時器",
                NotificationManager.IMPORTANCE_LOW // 使用 LOW 重要性減少干擾
            ).apply {
                description = "番茄鐘計時通知"
                setSound(null, null) // 禁用聲音
                enableVibration(false) // 禁用震動
                setShowBadge(false) // 不顯示角標
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            android.util.Log.d("PomodoroTimerService", "通知頻道創建完成")
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = Intent(this, PomodoroTimerService::class.java).apply {
            action = if (currentState == PomodoroState.RUNNING) ACTION_PAUSE_TIMER else ACTION_RESUME_TIMER
        }
        val pausePendingIntent = PendingIntent.getService(
            this, 1, pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, PomodoroTimerService::class.java).apply {
            action = ACTION_STOP_TIMER
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 2, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeText = formatTime(remainingTimeMs)
        val sessionText = if (sessionType == PomodoroType.FOCUS) "專注中" else "休息中"
        val stateText = when (currentState) {
            PomodoroState.RUNNING -> sessionText
            PomodoroState.PAUSED -> "已暫停"
            else -> sessionText
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🍅 番茄鐘 - $stateText")
            .setContentText(timeText)
            .setSmallIcon(R.drawable.ic_add) // 使用現有圖標
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true) // 靜音通知
            .setOnlyAlertOnce(true) // 只在第一次顯示時提醒
            .addAction(
                if (currentState == PomodoroState.RUNNING) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (currentState == PomodoroState.RUNNING) "暫停" else "繼續",
                pausePendingIntent
            )
            .addAction(android.R.drawable.ic_delete, "停止", stopPendingIntent)
            .build()
    }

    private fun updateNotification() {
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, createNotification())
        } catch (e: Exception) {
            android.util.Log.e("PomodoroTimerService", "更新通知失敗", e)
        }
    }

    private fun showCompletionNotification() {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val sessionText = if (sessionType == PomodoroType.FOCUS) "專注時間" else "休息時間"
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🎉 $sessionText 完成！")
            .setContentText("做得很好！繼續保持！")
            .setSmallIcon(R.drawable.ic_add)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID + 1, notification)
        } catch (e: Exception) {
            android.util.Log.e("PomodoroTimerService", "顯示完成通知失敗", e)
        }
    }

    private fun broadcastTimerUpdate() {
        val intent = Intent(BROADCAST_TIMER_UPDATE).apply {
            putExtra("remaining_time_ms", remainingTimeMs)
            putExtra("total_time_ms", totalDurationMs)
            putExtra("session_type", sessionType.name)
            putExtra("state", currentState.name)
        }
        sendBroadcast(intent)
    }

    private fun broadcastStateChanged() {
        val intent = Intent(BROADCAST_TIMER_STATE_CHANGED).apply {
            putExtra("state", currentState.name)
            putExtra("session_type", sessionType.name)
            putExtra("remaining_time_ms", remainingTimeMs)
        }
        sendBroadcast(intent)
    }

    private fun formatTime(timeMs: Long): String {
        val minutes = (timeMs / 1000 / 60).toInt()
        val seconds = ((timeMs / 1000) % 60).toInt()
        return String.format("%02d:%02d", minutes, seconds)
    }

    // Public methods for UI binding
    fun getCurrentState() = currentState
    fun getRemainingTime() = remainingTimeMs
    fun getTotalTime() = totalDurationMs
    fun getSessionType() = sessionType

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        serviceScope.cancel()
        android.util.Log.d("PomodoroTimerService", "服務銷毀")
    }
}