package com.example.doit2

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doit2.databinding.ActivityAchievementsBinding
import com.example.doit2.ui.adapter.AchievementAdapter
import com.example.doit2.ui.viewmodel.AchievementViewModel

class AchievementsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAchievementsBinding
    private val achievementViewModel: AchievementViewModel by viewModels()
    private lateinit var achievementAdapter: AchievementAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "🏆 成就獎勵"
    }

    private fun setupRecyclerView() {
        achievementAdapter = AchievementAdapter { achievement ->
            // 點擊成就的處理（可以顯示詳細資訊）
            // 暫時不做處理
        }

        binding.rvAchievements.apply {
            layoutManager = LinearLayoutManager(this@AchievementsActivity)
            adapter = achievementAdapter
        }
    }

    private fun setupObservers() {
        // 觀察所有成就
        achievementViewModel.allAchievements.observe(this) { achievements ->
            achievementAdapter.submitList(achievements)
            updateAchievementStats(achievements)
        }

        // 觀察用戶統計
        achievementViewModel.userStats.observe(this) { userStats ->
            userStats?.let { stats ->
                updateUserLevelUI(stats.totalPoints, stats.currentLevel)
            }
        }
    }

    private fun updateUserLevelUI(totalPoints: Int, currentLevel: Int) {
        with(binding) {
            // 顯示等級和積分
            tvUserLevel.text = achievementViewModel.getLevelName(currentLevel)
            tvTotalPoints.text = "$totalPoints 積分"

            // 計算並顯示等級進度
            val (progress, pointsToNext) = achievementViewModel.getLevelProgress(totalPoints)
            progressLevel.progress = progress

            if (currentLevel == 5) {
                tvProgressText.text = "🎉 已達最高等級！"
            } else {
                tvProgressText.text = "距離下一等級還需 $pointsToNext 積分"
            }
        }
    }

    private fun updateAchievementStats(achievements: List<com.example.doit2.data.model.Achievement>) {
        val unlockedCount = achievements.count { it.isUnlocked }
        val totalCount = achievements.size
        val completionRate = if (totalCount > 0) {
            (unlockedCount * 100 / totalCount)
        } else {
            0
        }

        with(binding) {
            tvUnlockedCount.text = unlockedCount.toString()
            tvTotalAchievements.text = totalCount.toString()
            tvCompletionRate.text = "$completionRate%"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}