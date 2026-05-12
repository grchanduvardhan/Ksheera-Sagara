package com.ksheera.sagara.ui.cowanalysis

import android.app.Application
import androidx.lifecycle.*
import com.ksheera.sagara.data.entity.Cow
import com.ksheera.sagara.data.repository.FarmRepository
import com.ksheera.sagara.utils.DateUtils
import kotlinx.coroutines.launch

data class CowProfitData(
    val cow: Cow,
    val totalIncome: Double,
    val totalExpense: Double,
    val netProfit: Double,
    val totalLiters: Double,
    val profitPerLiter: Double
)

class CowAnalysisViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FarmRepository(application)

    val activeCows = repository.getActiveCows()

    private val _cowProfitList = MutableLiveData<List<CowProfitData>>()
    val cowProfitList: LiveData<List<CowProfitData>> = _cowProfitList

    private val _startDate = MutableLiveData(DateUtils.currentMonthStart())
    private val _endDate = MutableLiveData(DateUtils.currentMonthEnd())

    init { loadCowAnalysis() }

    fun setDateRange(start: Long, end: Long) {
        _startDate.value = start
        _endDate.value = end
        loadCowAnalysis()
    }

    fun loadCowAnalysis() = viewModelScope.launch {
        val start = _startDate.value ?: return@launch
        val end = _endDate.value ?: return@launch
        val cows = repository.getActiveCowsSync()
        val result = cows.map { cow ->
            val income = repository.getIncomeByCow(cow.id, start, end)
            val expense = repository.getExpenseByCow(cow.id, start, end)
            val liters = repository.getLitersByCow(cow.id, start, end)
            val profit = income - expense
            val profitPerLiter = if (liters > 0) profit / liters else 0.0
            CowProfitData(cow, income, expense, profit, liters, profitPerLiter)
        }
        _cowProfitList.postValue(result.sortedByDescending { it.netProfit })
    }

    fun addCow(cow: Cow) = viewModelScope.launch {
        repository.insertCow(cow)
        loadCowAnalysis()
    }

    fun updateCow(cow: Cow) = viewModelScope.launch {
        repository.updateCow(cow)
        loadCowAnalysis()
    }

    fun deleteCow(cow: Cow) = viewModelScope.launch {
        repository.deleteCow(cow)
        loadCowAnalysis()
    }
}
