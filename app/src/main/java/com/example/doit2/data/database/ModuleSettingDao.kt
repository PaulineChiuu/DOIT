package com.example.doit2.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.doit2.data.model.ModuleSetting

@Dao
interface ModuleSettingDao {

    @Query("SELECT * FROM module_settings ORDER BY displayOrder ASC")
    fun getAllModuleSettings(): LiveData<List<ModuleSetting>>

    @Query("SELECT * FROM module_settings WHERE isEnabled = 1 ORDER BY displayOrder ASC")
    fun getEnabledModuleSettings(): LiveData<List<ModuleSetting>>

    @Query("SELECT * FROM module_settings WHERE moduleName = :moduleName")
    suspend fun getModuleSetting(moduleName: String): ModuleSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModuleSetting(moduleSetting: ModuleSetting)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModuleSettings(moduleSettings: List<ModuleSetting>)

    @Update
    suspend fun updateModuleSetting(moduleSetting: ModuleSetting)

    @Query("UPDATE module_settings SET isEnabled = :isEnabled WHERE moduleName = :moduleName")
    suspend fun updateModuleEnabled(moduleName: String, isEnabled: Boolean)

    @Query("UPDATE module_settings SET displayOrder = :displayOrder WHERE moduleName = :moduleName")
    suspend fun updateModuleDisplayOrder(moduleName: String, displayOrder: Int)
}