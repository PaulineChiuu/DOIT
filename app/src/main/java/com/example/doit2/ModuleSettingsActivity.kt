package com.example.doit2

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doit2.data.model.ModuleSetting
import com.example.doit2.databinding.ActivityModuleSettingsBinding
import com.example.doit2.ui.adapter.ModuleSettingsAdapter
import com.example.doit2.ui.viewmodel.ModuleSettingViewModel

class ModuleSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModuleSettingsBinding
    private val moduleSettingViewModel: ModuleSettingViewModel by viewModels()
    private lateinit var moduleSettingsAdapter: ModuleSettingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModuleSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "模組設定"
    }

    private fun setupRecyclerView() {
        moduleSettingsAdapter = ModuleSettingsAdapter { moduleSetting, isEnabled ->
            onModuleToggle(moduleSetting, isEnabled)
        }

        binding.rvModuleSettings.apply {
            layoutManager = LinearLayoutManager(this@ModuleSettingsActivity)
            adapter = moduleSettingsAdapter
        }
    }

    private fun setupObservers() {
        moduleSettingViewModel.allModuleSettings.observe(this) { modules ->
            moduleSettingsAdapter.submitList(modules)
            updateUI(modules)
        }
    }

    private fun updateUI(modules: List<ModuleSetting>) {
        val enabledCount = modules.count { it.isEnabled }
        val totalCount = modules.size

        binding.tvModuleStatus.text = "已啟用 $enabledCount / $totalCount 個模組"

        // 顯示提示
        if (enabledCount == 0) {
            binding.tvHint.text = "選擇你想要的功能模組，讓 Do It 更符合你的需求"
        } else {
            binding.tvHint.text = "已選擇的模組會顯示在主頁面上"
        }
    }

    private fun onModuleToggle(moduleSetting: ModuleSetting, isEnabled: Boolean) {
        // 更新模組狀態
        moduleSettingViewModel.updateModuleEnabled(moduleSetting.moduleName, isEnabled)

        // 顯示提示訊息
        val moduleDisplayName = getModuleDisplayName(moduleSetting.moduleName)
        val message = if (isEnabled) {
            "已啟用「$moduleDisplayName」"
        } else {
            "已關閉「$moduleDisplayName」"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun getModuleDisplayName(moduleName: String): String {
        return when (moduleName) {
            "tasks_goal" -> "任務目標"
            "calendar" -> "日曆追蹤"
            "self_talk" -> "自我對話"
            "pomodoro" -> "番茄鐘"
            "achievements" -> "成就獎勵"
            "meditation" -> "靜心倒數"
            "music" -> "習慣配樂"
            else -> "未知模組"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}