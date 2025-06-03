package com.example.doit2.data.repository

import androidx.lifecycle.LiveData
import com.example.doit2.data.database.ModuleSettingDao
import com.example.doit2.data.model.ModuleSetting

class ModuleSettingRepository(private val moduleSettingDao: ModuleSettingDao) {

    fun getAllModuleSettings(): LiveData<List<ModuleSetting>> {
        return moduleSettingDao.getAllModuleSettings()
    }

    fun getEnabledModuleSettings(): LiveData<List<ModuleSetting>> {
        return moduleSettingDao.getEnabledModuleSettings()
    }

    suspend fun getModuleSetting(moduleName: String): ModuleSetting? {
        return moduleSettingDao.getModuleSetting(moduleName)
    }

    suspend fun insertModuleSetting(moduleSetting: ModuleSetting) {
        moduleSettingDao.insertModuleSetting(moduleSetting)
    }

    suspend fun updateModuleSetting(moduleSetting: ModuleSetting) {
        moduleSettingDao.updateModuleSetting(moduleSetting)
    }

    suspend fun updateModuleEnabled(moduleName: String, isEnabled: Boolean) {
        moduleSettingDao.updateModuleEnabled(moduleName, isEnabled)
    }

    suspend fun updateModuleDisplayOrder(moduleName: String, displayOrder: Int) {
        moduleSettingDao.updateModuleDisplayOrder(moduleName, displayOrder)
    }

    suspend fun initializeDefaultModules() {
        val defaultModules = listOf(
            ModuleSetting("tasks_goal", false, 0),
            ModuleSetting("calendar", false, 1),
            ModuleSetting("self_talk", false, 2),
            ModuleSetting("achievements", false, 3),
            ModuleSetting("meditation", false, 4),
            ModuleSetting("music", false, 5)
        )

        // 檢查是否已初始化，避免重複插入
        val existingModule = moduleSettingDao.getModuleSetting("tasks_goal")
        if (existingModule == null) {
            moduleSettingDao.insertModuleSettings(defaultModules)
        }
    }
}