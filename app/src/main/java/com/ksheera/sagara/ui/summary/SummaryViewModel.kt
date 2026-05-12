package com.ksheera.sagara.ui.summary

import android.app.Application
import androidx.lifecycle.*
import com.ksheera.sagara.data.entity.ExpenseEntry
import com.ksheera.sagara.data.entity.MilkEntry
import com.ksheera.sagara.data.repository.FarmRepository
import com.ksheera.sagara.utils.DateUtils
import kotlinx.coroutines.launch

class SummaryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FarmRepository(application)

    private val _selectedYear = MutableLiveData(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR))
    private val _selectedMonth = MutableLiveData(java.util.Calendar.getInstance().get(java.util.Calendar.MONTH))

    val selectedYear: LiveData<Int> = _selectedYear
    val selectedMonth: LiveData<Int> = _selectedMonth

    private val _milkEntries = MutableLiveData<List<MilkEntry>>()
    val milkEntries: LiveData<List<MilkEntry>> = _milkEntries

    private val _expenseEntries = MutableLiveData<List<ExpenseEntry>>()
    val expenseEntries: LiveData<List<ExpenseEntry>> = _expenseEntries

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init { loadData() }

    fun setMonthYear(year: Int, month: Int) {
        _selectedYear.value = year
        _selectedMonth.value = month
        loadData()
    }

    fun loadData() = viewModelScope.launch {
        _isLoading.postValue(true)
        val year = _selectedYear.value ?: return@launch
        val month = _selectedMonth.value ?: return@launch
        val start = DateUtils.startOfMonth(year, month)
        val end = DateUtils.endOfMonth(year, month)
        _milkEntries.postValue(repository.getMilkEntriesSync(start, end))
        _expenseEntries.postValue(repository.getExpensesSync(start, end))
        _isLoading.postValue(false)
    }

    fun getMonthLabel(): String {
        val year = _selectedYear.value ?: return ""
        val month = _selectedMonth.value ?: return ""
        val cal = java.util.Calendar.getInstance()
        cal.set(year, month, 1)
        return DateUtils.formatMonthYear(cal.timeInMillis)
    }
}
