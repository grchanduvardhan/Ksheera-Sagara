package com.ksheera.sagara.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.ksheera.sagara.data.database.AppDatabase
import com.ksheera.sagara.data.entity.Cow
import com.ksheera.sagara.data.entity.ExpenseEntry
import com.ksheera.sagara.data.entity.MilkEntry

class FarmRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val milkDao = db.milkEntryDao()
    private val expenseDao = db.expenseEntryDao()
    private val cowDao = db.cowDao()

    // ─── Milk / Income ───────────────────────────────────────────────
    suspend fun insertMilkEntry(entry: MilkEntry) = milkDao.insert(entry)
    suspend fun updateMilkEntry(entry: MilkEntry) = milkDao.update(entry)
    suspend fun deleteMilkEntry(entry: MilkEntry) = milkDao.delete(entry)
    fun getAllMilkEntries(): LiveData<List<MilkEntry>> = milkDao.getAllEntries()
    fun getMilkEntriesByRange(start: Long, end: Long) = milkDao.getEntriesByDateRange(start, end)
    fun getTotalIncome(start: Long, end: Long) = milkDao.getTotalIncome(start, end)
    fun getTotalLiters(start: Long, end: Long) = milkDao.getTotalLiters(start, end)
    fun getAverageFat(start: Long, end: Long) = milkDao.getAverageFat(start, end)
    fun getMilkByCow(cowId: Long) = milkDao.getEntriesByCow(cowId)
    suspend fun getMilkEntriesSync(start: Long, end: Long) = milkDao.getEntriesByDateRangeSync(start, end)
    suspend fun getAllMilkEntriesSync() = milkDao.getAllEntriesSync()

    // ─── Expenses ────────────────────────────────────────────────────
    suspend fun insertExpense(entry: ExpenseEntry) = expenseDao.insert(entry)
    suspend fun updateExpense(entry: ExpenseEntry) = expenseDao.update(entry)
    suspend fun deleteExpense(entry: ExpenseEntry) = expenseDao.delete(entry)
    fun getAllExpenses(): LiveData<List<ExpenseEntry>> = expenseDao.getAllEntries()
    fun getExpensesByRange(start: Long, end: Long) = expenseDao.getEntriesByDateRange(start, end)
    fun getTotalExpense(start: Long, end: Long) = expenseDao.getTotalExpense(start, end)
    fun getExpenseByCategory(cat: String, start: Long, end: Long) = expenseDao.getTotalByCategory(cat, start, end)
    suspend fun getExpensesSync(start: Long, end: Long) = expenseDao.getEntriesByDateRangeSync(start, end)
    suspend fun getAllExpensesSync() = expenseDao.getAllEntriesSync()
    suspend fun getTotalExpenseSync(start: Long, end: Long) = expenseDao.getTotalExpenseSync(start, end)

    // ─── Cows ────────────────────────────────────────────────────────
    suspend fun insertCow(cow: Cow) = cowDao.insert(cow)
    suspend fun updateCow(cow: Cow) = cowDao.update(cow)
    suspend fun deleteCow(cow: Cow) = cowDao.delete(cow)
    fun getActiveCows(): LiveData<List<Cow>> = cowDao.getActiveCows()
    fun getAllCows(): LiveData<List<Cow>> = cowDao.getAllCows()
    suspend fun getActiveCowsSync() = cowDao.getActiveCowsSync()
    suspend fun getCowById(id: Long) = cowDao.getCowById(id)

    // ─── Cow Analysis ────────────────────────────────────────────────
    suspend fun getIncomeByCow(cowId: Long, start: Long, end: Long) =
        milkDao.getTotalIncomeByCow(cowId, start, end) ?: 0.0

    suspend fun getLitersByCow(cowId: Long, start: Long, end: Long) =
        milkDao.getTotalLitersByCow(cowId, start, end) ?: 0.0

    suspend fun getExpenseByCow(cowId: Long, start: Long, end: Long) =
        expenseDao.getTotalExpenseByCow(cowId, start, end) ?: 0.0
}
