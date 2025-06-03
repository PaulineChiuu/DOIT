package com.example.doit2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.doit2.data.database.Converters
import java.util.Date

@Entity(tableName = "achievements")
@TypeConverters(Converters::class)
data class Achievement(
    @PrimaryKey
    val id: String,                    // "first_task", "streak_3_days", etc.
    val title: String,                 // "完成首個任務"
    val description: String,           // "踏出第一步，開始你的效率之旅"
    val iconRes: Int,                  // Android drawable resource
    val points: Int,                   // 積分獎勵
    val category: String,              // "task", "streak", "module", "special"
    val isUnlocked: Boolean = false,   // 是否已解鎖
    val unlockedDate: Date? = null     // 解鎖時間
)