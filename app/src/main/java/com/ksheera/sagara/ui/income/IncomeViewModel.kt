package com.ksheera.sagara.ui.income

import android.app.Application
import androidx.lifecycle.*
import com.ksheera.sagara.data.entity.MilkEntry
import com.ksheera.sagara.data.repository.FarmRepository
import com.ksheera.sagara.utils.DateUtils
import kotlinx.coroutines.launch

class IncomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FarmRepository(application)

    val allEntries = repository.getAllMilkEntries()
    val activeCows = repository.getActiveCows()

    private val _startDate = MutableLiveData(DateUtils.currentMonthStart())
    private val _endDate = MutableLiveData(DateUtils.currentMonthEnd())

    val filteredEntries: LiveData<List<MilkEntry>> = _startDate.switchMap { start ->
        _endDate.switchMap { end ->
            repository.getMilkEntriesByRange(start, end)
        }
    }

    fun setDateRange(start: Long, end: Long) {
        _startDate.value = start
        _endDate.value = end
    }

    fun addEntry(entry: MilkEntry) = viewModelScope.launch {
        repository.insertMilkEntry(entry)
    }

    fun updateEntry(entry: MilkEntry) = viewModelScope.launch {
        repository.updateMilkEntry(entry)
    }

    fun deleteEntry(entry: MilkEntry) = viewModelScope.launch {
        repository.deleteMilkEntry(entry)
    }
}
