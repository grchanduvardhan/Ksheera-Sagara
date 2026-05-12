package com.ksheera.sagara.ui.expense

import android.app.Application
import androidx.lifecycle.*
import com.ksheera.sagara.data.entity.ExpenseEntry
import com.ksheera.sagara.data.repository.FarmRepository
import com.ksheera.sagara.utils.DateUtils
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FarmRepository(application)

    val allEntries = repository.getAllExpenses()
    val activeCows = repository.getActiveCows()

    private val _startDate = MutableLiveData(DateUtils.currentMonthStart())
    private val _endDate = MutableLiveData(DateUtils.currentMonthEnd())

    val filteredEntries: LiveData<List<ExpenseEntry>> = _startDate.switchMap { start ->
        _endDate.switchMap { end ->
            repository.getExpensesByRange(start, end)
        }
    }

    fun setDateRange(start: Long, end: Long) {
        _startDate.value = start
        _endDate.value = end
    }

    fun addEntry(entry: ExpenseEntry) = viewModelScope.launch {
        repository.insertExpense(entry)
    }

    fun updateEntry(entry: ExpenseEntry) = viewModelScope.launch {
        repository.updateExpense(entry)
    }

    fun deleteEntry(entry: ExpenseEntry) = viewModelScope.launch {
        repository.deleteExpense(entry)
    }
}
