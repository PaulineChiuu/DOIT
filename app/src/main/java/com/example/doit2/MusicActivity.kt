package com.example.doit2

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doit2.databinding.ActivityMusicBinding
import com.example.doit2.service.MusicService
import com.example.doit2.ui.adapter.MusicTrackAdapter

class MusicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMusicBinding
    private var musicService: MusicService? = null
    private var isBound = false
    private lateinit var musicTrackAdapter: MusicTrackAdapter

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
            setupMusicService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            musicService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupControls()
        bindMusicService()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "習慣配樂"
    }

    private fun setupRecyclerView() {
        val musicTracks = listOf(
            MusicTrack("專注音樂", "幫助集中注意力的輕音樂", R.raw.focus_music, false),
            MusicTrack("放鬆音樂", "舒緩壓力的輕柔音樂", R.raw.relax_music, false),
            MusicTrack("自然音效", "森林鳥鳴與流水聲", R.raw.nature_sounds, false),
            MusicTrack("雨聲", "溫和的雨滴聲音", R.raw.rain_sounds, false)
        )

        musicTrackAdapter = MusicTrackAdapter(musicTracks) { track ->
            onTrackSelected(track)
        }

        binding.rvMusicTracks.apply {
            layoutManager = LinearLayoutManager(this@MusicActivity)
            adapter = musicTrackAdapter
        }
    }

    private fun setupControls() {
        binding.btnPlay.setOnClickListener {
            musicService?.playMusic()
            updatePlayButton(true)
        }

        binding.btnPause.setOnClickListener {
            musicService?.pauseMusic()
            updatePlayButton(false)
        }

        binding.btnStop.setOnClickListener {
            musicService?.stopMusic()
            updatePlayButton(false)
            musicTrackAdapter.clearSelection()
        }

        binding.seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val volume = progress / 100f
                    musicService?.setVolume(volume)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun bindMusicService() {
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun setupMusicService() {
        musicService?.setOnMusicCompleteListener {
            runOnUiThread {
                updatePlayButton(false)
                musicTrackAdapter.clearSelection()
            }
        }
    }

    private fun onTrackSelected(track: MusicTrack) {
        musicService?.let { service ->
            service.loadMusic(track.resourceId)
            binding.tvCurrentTrack.text = track.name
            binding.tvTrackDescription.text = track.description
            musicTrackAdapter.setSelectedTrack(track)

            // 如果音樂服務正在播放，自動播放新選擇的音樂
            if (service.isPlaying()) {
                service.playMusic()
            }
        }
    }

    private fun updatePlayButton(isPlaying: Boolean) {
        binding.btnPlay.isEnabled = !isPlaying
        binding.btnPause.isEnabled = isPlaying
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    data class MusicTrack(
        val name: String,
        val description: String,
        val resourceId: Int,
        var isSelected: Boolean = false
    )
}