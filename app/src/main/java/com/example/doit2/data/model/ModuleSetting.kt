package com.example.doit2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "module_settings")
data class ModuleSetting(
    @PrimaryKey
    val moduleName: String,  // "tasks_goal", "calendar", "self_talk", "achievements", "meditation", "music"
    val isEnabled: Boolean = false,
    val displayOrder: Int = 0
)