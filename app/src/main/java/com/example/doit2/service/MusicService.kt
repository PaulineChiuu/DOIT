package com.example.doit2.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import com.example.doit2.R

class MusicService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private val binder = MusicBinder()
    private var onMusicCompleteListener: (() -> Unit)? = null

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    fun loadMusic(resourceId: Int) {
        try {
            // 停止並釋放當前的 MediaPlayer
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }

            // 創建新的 MediaPlayer
            mediaPlayer = MediaPlayer.create(this, resourceId).apply {
                isLooping = true // 循環播放
                setOnCompletionListener {
                    onMusicCompleteListener?.invoke()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playMusic() {
        try {
            mediaPlayer?.let {
                if (!it.isPlaying) {
                    it.start()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pauseMusic() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopMusic() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                // 重新 prepare 以便下次播放
                it.prepare()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setVolume(volume: Float) {
        try {
            mediaPlayer?.setVolume(volume, volume)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun setOnMusicCompleteListener(listener: () -> Unit) {
        onMusicCompleteListener = listener
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }
}