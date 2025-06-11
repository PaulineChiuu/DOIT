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

    suspend fun insertModuleSettings(moduleSettings: List<ModuleSetting>) {
        moduleSettingDao.insertModuleSettings(moduleSettings)
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

    // 初始化預設模組，只執行一次
    suspend fun initializeDefaultModules() {
        val defaultModules = listOf(
            ModuleSetting("tasks_goal", false, 0),
            ModuleSetting("calendar", false, 1),
            ModuleSetting("self_talk", false, 2),
            ModuleSetting("achievements", false, 3),
            ModuleSetting("meditation", false, 4),
            ModuleSetting("music", false, 5),
            ModuleSetting("pomodoro", false, 6),
            ModuleSetting("taiwan_tour_map", false, 7)
        )

        val isInitialized = moduleSettingDao.getModuleSetting("tasks_goal") != null
        if (!isInitialized) {
            moduleSettingDao.insertModuleSettings(defaultModules)
        }
    }
}
