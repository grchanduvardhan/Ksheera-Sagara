package com.ksheera.sagara.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ksheera.sagara.data.entity.ExpenseEntry

@Dao
interface ExpenseEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ExpenseEntry): Long

    @Update
    suspend fun update(entry: ExpenseEntry)

    @Delete
    suspend fun delete(entry: ExpenseEntry)

    @Query("SELECT * FROM expense_entries ORDER BY date DESC")
    fun getAllEntries(): LiveData<List<ExpenseEntry>>

    @Query("SELECT * FROM expense_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getEntriesByDateRange(startDate: Long, endDate: Long): LiveData<List<ExpenseEntry>>

    @Query("SELECT SUM(amount) FROM expense_entries WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalExpense(startDate: Long, endDate: Long): LiveData<Double?>

    @Query("SELECT SUM(amount) FROM expense_entries WHERE category = :category AND date BETWEEN :startDate AND :endDate")
    fun getTotalByCategory(category: String, startDate: Long, endDate: Long): LiveData<Double?>

    @Query("SELECT SUM(amount) FROM expense_entries WHERE cowId = :cowId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalExpenseByCow(cowId: Long, startDate: Long, endDate: Long): Double?

    @Query("SELECT * FROM expense_entries ORDER BY date DESC")
    suspend fun getAllEntriesSync(): List<ExpenseEntry>

    @Query("SELECT * FROM expense_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getEntriesByDateRangeSync(startDate: Long, endDate: Long): List<ExpenseEntry>

    @Query("SELECT SUM(amount) FROM expense_entries WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalExpenseSync(startDate: Long, endDate: Long): Double?
}
