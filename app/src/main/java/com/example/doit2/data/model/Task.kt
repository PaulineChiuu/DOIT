package com.example.doit2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.doit2.data.database.Converters
import java.util.Date

@Entity(tableName = "tasks")
@TypeConverters(Converters::class)
data class  Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Date = Date()
)