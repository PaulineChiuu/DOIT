package com.example.doit2.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.doit2.data.database.AppDatabase
import com.example.doit2.data.model.ModuleSetting
import com.example.doit2.data.repository.ModuleSettingRepository
import kotlinx.coroutines.launch

class ModuleSettingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ModuleSettingRepository
    val allModuleSettings: LiveData<List<ModuleSetting>>
    val enabledModuleSettings: LiveData<List<ModuleSetting>>

    init {
        val moduleSettingDao = AppDatabase.getDatabase(application).moduleSettingDao()
        repository = ModuleSettingRepository(moduleSettingDao)
        allModuleSettings = repository.getAllModuleSettings()
        enabledModuleSettings = repository.getEnabledModuleSettings()

        // 初始化預設模組 (要在 coroutine 中呼叫 suspend 函式)
        viewModelScope.launch {
            repository.initializeDefaultModules()
        }
    }

    fun updateModuleEnabled(moduleName: String, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.updateModuleEnabled(moduleName, isEnabled)
        }
    }

    fun updateModuleDisplayOrder(moduleName: String, displayOrder: Int) {
        viewModelScope.launch {
            repository.updateModuleDisplayOrder(moduleName, displayOrder)
        }
    }

    fun updateModuleSetting(moduleSetting: ModuleSetting) {
        viewModelScope.launch {
            repository.updateModuleSetting(moduleSetting)
        }
    }

    suspend fun getModuleSetting(moduleName: String): ModuleSetting? {
        return repository.getModuleSetting(moduleName)
    }
}
