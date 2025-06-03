package com.example.doit2.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.doit2.data.model.Task

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY createdAt DESC")
    fun getIncompleteTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY createdAt DESC")
    fun getCompletedTasks(): LiveData<List<Task>>

    @Insert
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    @Query("SELECT COUNT(*) FROM tasks WHERE date(createdAt/1000, 'unixepoch', 'localtime') = :date")
    suspend fun getTasksCountByDate(date: String): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE date(createdAt/1000, 'unixepoch', 'localtime') = :date AND isCompleted = 1")
    suspend fun getCompletedTasksCountByDate(date: String): Int

    @Query("SELECT * FROM tasks WHERE date(createdAt/1000, 'unixepoch', 'localtime') = :date")
    suspend fun getTasksByDate(date: String): List<Task>

    @Query("SELECT * FROM tasks WHERE date(createdAt/1000, 'unixepoch', 'localtime') = :date AND isCompleted = 1")
    suspend fun getCompletedTasksByDate(date: String): List<Task>

    // 添加調試查詢方法
    @Query("SELECT id, title, createdAt, isCompleted, date(createdAt/1000, 'unixepoch', 'localtime') as dateStr FROM tasks ORDER BY createdAt DESC LIMIT 10")
    suspend fun getRecentTasksWithDate(): List<TaskWithDate>

    // 添加數據類來幫助調試
    data class TaskWithDate(
        val id: Long,
        val title: String,
        val createdAt: Long,
        val isCompleted: Boolean,
        val dateStr: String
    )
}