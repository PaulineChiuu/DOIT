package com.example.doit2.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.doit2.data.model.DailyRecord

@Dao
interface DailyRecordDao {

    @Query("SELECT * FROM daily_records WHERE date = :date")
    suspend fun getRecordByDate(date: String): DailyRecord?

    @Query("SELECT * FROM daily_records WHERE date = :date")
    fun getRecordByDateLive(date: String): LiveData<DailyRecord?>

    @Query("SELECT * FROM daily_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    suspend fun getRecordsBetweenDates(startDate: String, endDate: String): List<DailyRecord>

    @Query("SELECT * FROM daily_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    fun getRecordsBetweenDatesLive(startDate: String, endDate: String): LiveData<List<DailyRecord>>

    @Query("SELECT * FROM daily_records WHERE date LIKE :monthPattern ORDER BY date")
    suspend fun getRecordsByMonth(monthPattern: String): List<DailyRecord>

    @Query("SELECT * FROM daily_records WHERE date LIKE :monthPattern ORDER BY date")
    fun getRecordsByMonthLive(monthPattern: String): LiveData<List<DailyRecord>>

    @Query("SELECT * FROM daily_records ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentRecords(limit: Int): List<DailyRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DailyRecord)

    @Update
    suspend fun updateRecord(record: DailyRecord)

    @Query("DELETE FROM daily_records WHERE date = :date")
    suspend fun deleteRecordByDate(date: String)

    @Query("DELETE FROM daily_records")
    suspend fun deleteAllRecords()

    // 統計查詢
    @Query("SELECT COUNT(*) FROM daily_records WHERE completedTasks > 0")
    suspend fun getActiveDaysCount(): Int

    @Query("SELECT COUNT(*) FROM daily_records WHERE completedTasks = totalTasks AND totalTasks > 0")
    suspend fun getPerfectDaysCount(): Int

    @Query("SELECT AVG(CAST(completedTasks AS FLOAT) / CASE WHEN totalTasks = 0 THEN 1 ELSE totalTasks END * 100) FROM daily_records WHERE totalTasks > 0")
    suspend fun getAverageCompletionRate(): Float

    @Query("SELECT SUM(completedTasks) FROM daily_records")
    suspend fun getTotalCompletedTasks(): Int

    @Query("SELECT SUM(meditationMinutes) FROM daily_records")
    suspend fun getTotalMeditationMinutes(): Int

    // 連續天數計算相關
    @Query("SELECT * FROM daily_records WHERE completedTasks > 0 ORDER BY date DESC")
    suspend fun getActiveDaysOrderByDate(): List<DailyRecord>

    @Query("SELECT * FROM daily_records WHERE date >= :fromDate ORDER BY date ASC")
    suspend fun getRecordsFromDate(fromDate: String): List<DailyRecord>
}