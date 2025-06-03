package com.example.doit2.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.doit2.data.model.Task
import com.example.doit2.data.model.ModuleSetting
import com.example.doit2.data.model.Achievement
import com.example.doit2.data.model.UserStats
import com.example.doit2.data.model.DailyRecord

@Database(
    entities = [Task::class, ModuleSetting::class, Achievement::class, UserStats::class, DailyRecord::class],
    version = 4,  // 版本號從 3 升級到 4
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun moduleSettingDao(): ModuleSettingDao
    abstract fun achievementDao(): AchievementDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun dailyRecordDao(): DailyRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // 資料庫遷移：從版本 2 到版本 3
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    // 創建 achievements 表
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS achievements (
                            id TEXT PRIMARY KEY NOT NULL,
                            title TEXT NOT NULL,
                            description TEXT NOT NULL,
                            iconRes INTEGER NOT NULL,
                            points INTEGER NOT NULL,
                            category TEXT NOT NULL,
                            isUnlocked INTEGER NOT NULL DEFAULT 0,
                            unlockedDate INTEGER
                        )
                    """)

                    // 創建 user_stats 表
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS user_stats (
                            id INTEGER PRIMARY KEY NOT NULL DEFAULT 1,
                            totalPoints INTEGER NOT NULL DEFAULT 0,
                            currentLevel INTEGER NOT NULL DEFAULT 1,
                            completedTasks INTEGER NOT NULL DEFAULT 0,
                            currentStreak INTEGER NOT NULL DEFAULT 0,
                            longestStreak INTEGER NOT NULL DEFAULT 0,
                            lastActiveDate INTEGER NOT NULL,
                            meditationMinutes INTEGER NOT NULL DEFAULT 0,
                            musicUsageCount INTEGER NOT NULL DEFAULT 0,
                            pomodoroSessions INTEGER NOT NULL DEFAULT 0,
                            modulesUnlocked TEXT NOT NULL DEFAULT '',
                            todayCompletedTasks INTEGER NOT NULL DEFAULT 0,
                            todayActiveDate INTEGER NOT NULL
                        )
                    """)

                    // 插入初始 UserStats 記錄
                    db.execSQL("""
                        INSERT OR IGNORE INTO user_stats (
                            id, totalPoints, currentLevel, completedTasks, currentStreak, 
                            longestStreak, lastActiveDate, meditationMinutes, musicUsageCount, 
                            pomodoroSessions, modulesUnlocked, todayCompletedTasks, todayActiveDate
                        ) VALUES (
                            1, 0, 1, 0, 0, 0, ${System.currentTimeMillis()}, 0, 0, 0, '', 0, ${System.currentTimeMillis()}
                        )
                    """)

                } catch (e: Exception) {
                    e.printStackTrace()
                    throw e
                }
            }
        }

        // 資料庫遷移：從版本 3 到版本 4 (新增日曆追蹤)
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    // 創建 daily_records 表
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS daily_records (
                            date TEXT PRIMARY KEY NOT NULL,
                            totalTasks INTEGER NOT NULL DEFAULT 0,
                            completedTasks INTEGER NOT NULL DEFAULT 0,
                            appUsed INTEGER NOT NULL DEFAULT 0,
                            firstTaskTime INTEGER,
                            lastTaskTime INTEGER,
                            meditationMinutes INTEGER NOT NULL DEFAULT 0,
                            musicUsed INTEGER NOT NULL DEFAULT 0,
                            pomodoroSessions INTEGER NOT NULL DEFAULT 0,
                            recordedAt INTEGER NOT NULL
                        )
                    """)

                    // 插入今日初始記錄
                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    db.execSQL("""
                        INSERT OR IGNORE INTO daily_records (
                            date, totalTasks, completedTasks, appUsed, meditationMinutes, 
                            musicUsed, pomodoroSessions, recordedAt
                        ) VALUES (
                            '$today', 0, 0, 1, 0, 0, 0, ${System.currentTimeMillis()}
                        )
                    """)

                } catch (e: Exception) {
                    e.printStackTrace()
                    throw e
                }
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "doit_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)  // 添加兩個遷移策略
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}