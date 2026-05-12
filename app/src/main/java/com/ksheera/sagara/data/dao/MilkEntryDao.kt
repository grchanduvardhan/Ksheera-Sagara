package com.ksheera.sagara.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ksheera.sagara.data.entity.MilkEntry

@Dao
interface MilkEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MilkEntry): Long

    @Update
    suspend fun update(entry: MilkEntry)

    @Delete
    suspend fun delete(entry: MilkEntry)

    @Query("SELECT * FROM milk_entries ORDER BY date DESC")
    fun getAllEntries(): LiveData<List<MilkEntry>>

    @Query("SELECT * FROM milk_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getEntriesByDateRange(startDate: Long, endDate: Long): LiveData<List<MilkEntry>>

    @Query("SELECT SUM(totalPayment) FROM milk_entries WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalIncome(startDate: Long, endDate: Long): LiveData<Double?>

    @Query("SELECT SUM(liters) FROM milk_entries WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalLiters(startDate: Long, endDate: Long): LiveData<Double?>

    @Query("SELECT AVG(fatPercentage) FROM milk_entries WHERE date BETWEEN :startDate AND :endDate")
    fun getAverageFat(startDate: Long, endDate: Long): LiveData<Double?>

    @Query("SELECT * FROM milk_entries WHERE cowId = :cowId ORDER BY date DESC")
    fun getEntriesByCow(cowId: Long): LiveData<List<MilkEntry>>

    @Query("SELECT SUM(totalPayment) FROM milk_entries WHERE cowId = :cowId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalIncomeByCow(cowId: Long, startDate: Long, endDate: Long): Double?

    @Query("SELECT SUM(liters) FROM milk_entries WHERE cowId = :cowId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalLitersByCow(cowId: Long, startDate: Long, endDate: Long): Double?

    @Query("SELECT * FROM milk_entries ORDER BY date DESC")
    suspend fun getAllEntriesSync(): List<MilkEntry>

    @Query("SELECT * FROM milk_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getEntriesByDateRangeSync(startDate: Long, endDate: Long): List<MilkEntry>
}
