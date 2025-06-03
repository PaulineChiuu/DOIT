package com.example.doit2.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.doit2.data.model.Task
import com.example.doit2.data.model.ModuleSetting

@Database(
    entities = [Task::class, ModuleSetting::class],
    version = 2,  // 版本號從 1 升級到 2
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun moduleSettingDao(): ModuleSettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "doit_database"
                )
                    .fallbackToDestructiveMigration()  // 開發階段允許刪除重建
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}