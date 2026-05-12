package com.ksheera.sagara.ui.dashboard

import android.app.Application
import androidx.lifecycle.*
import com.ksheera.sagara.data.repository.FarmRepository
import com.ksheera.sagara.utils.DateUtils
import kotlinx.coroutines.launch

data class DashboardData(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netProfit: Double = 0.0,
    val totalLiters: Double = 0.0,
    val profitPerLiter: Double = 0.0,
    val avgFat: Double = 0.0,
    val fodderExpense: Double = 0.0,
    val medicalExpense: Double = 0.0,
    val laborExpense: Double = 0.0,
    val otherExpense: Double = 0.0,
    val monthLabel: String = ""
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FarmRepository(application)

    private val _selectedYear = MutableLiveData(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR))
    private val _selectedMonth = MutableLiveData(java.util.Calendar.getInstance().get(java.util.Calendar.MONTH))

    val selectedYear: LiveData<Int> = _selectedYear
    val selectedMonth: LiveData<Int> = _selectedMonth

    private val _dashboardData = MutableLiveData<DashboardData>()
    val dashboardData: LiveData<DashboardData> = _dashboardData

    init { loadData() }

    fun setMonthYear(year: Int, month: Int) {
        _selectedYear.value = year
        _selectedMonth.value = month
        loadData()
    }

    fun loadData() = viewModelScope.launch {
        val year = _selectedYear.value ?: return@launch
        val month = _selectedMonth.value ?: return@launch
        val start = DateUtils.startOfMonth(year, month)
        val end = DateUtils.endOfMonth(year, month)

        val milkEntries = repository.getMilkEntriesSync(start, end)
        val expenseEntries = repository.getExpensesSync(start, end)

        val totalIncome = milkEntries.sumOf { it.totalPayment }
        val totalExpense = expenseEntries.sumOf { it.amount }
        val netProfit = totalIncome - totalExpense
        val totalLiters = milkEntries.sumOf { it.liters }
        val profitPerLiter = if (totalLiters > 0) netProfit / totalLiters else 0.0
        val avgFat = if (milkEntries.isNotEmpty()) milkEntries.map { it.fatPercentage }.average() else 0.0

        val fodder = expenseEntries.filter { it.category == "Fodder" }.sumOf { it.amount }
        val medical = expenseEntries.filter { it.category == "Medical" }.sumOf { it.amount }
        val labor = expenseEntries.filter { it.category == "Labor" }.sumOf { it.amount }
        val other = expenseEntries.filter { it.category == "Other" }.sumOf { it.amount }

        val cal = java.util.Calendar.getInstance()
        cal.set(year, month, 1)
        val label = DateUtils.formatMonthYear(cal.timeInMillis)

        _dashboardData.postValue(DashboardData(
            totalIncome, totalExpense, netProfit,
            totalLiters, profitPerLiter, avgFat,
            fodder, medical, labor, other, label
        ))
    }
}
