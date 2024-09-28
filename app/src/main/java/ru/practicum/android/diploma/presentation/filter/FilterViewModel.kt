package ru.practicum.android.diploma.presentation.filter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.practicum.android.diploma.domain.FilterInteractor
import ru.practicum.android.diploma.domain.models.Filter

class FilterViewModel(private val filterInteractor: FilterInteractor, application: Application) :
    AndroidViewModel(application) {
    private var currentFilter = filterInteractor.currentFilter()
    private val stateLiveData = MutableLiveData<FilterState>()
    fun observeState(): LiveData<FilterState> = stateLiveData

    init {
        postCurrentFilter()
    }

    private fun postCurrentFilter() {
        if (currentFilter == Filter()) {
            stateLiveData.postValue(FilterState.Empty)
        } else {
            stateLiveData.postValue(FilterState.Filled(currentFilter))
        }
    }

    fun clearFilter() {
        filterInteractor.flushCurrentFilter()
        currentFilter = filterInteractor.currentFilter()
        postCurrentFilter()
    }

    fun setSalaryIsRequired(required: Boolean) {
        filterInteractor.setOnlyWithSalary(required)
        currentFilter = filterInteractor.currentFilter()
    }

    fun setSalary(salary: String) {
        if (salary.isEmpty()) {
            clearSalary()
        } else {
            filterInteractor.setSalary(salary)
            currentFilter = filterInteractor.currentFilter()
        }
    }

    fun clearSalary() {
        filterInteractor.setSalary(null)
        currentFilter = filterInteractor.currentFilter()
    }

    fun clearWorkplace() {
        filterInteractor.setCountry(null)
        filterInteractor.setArea(null)
        currentFilter = filterInteractor.currentFilter()
    }

    fun clearIndustry() {
        filterInteractor.setIndustry(null)
        currentFilter = filterInteractor.currentFilter()
    }

    fun applyFilter() {
        filterInteractor.apply()
    }

    fun checkFilter() {
        currentFilter = filterInteractor.currentFilter()
        postCurrentFilter()
    }

    fun currentFilterIsEmpty() = currentFilter == Filter()

    fun currentFilterChanged() = currentFilter != filterInteractor.appliedFilter()
}
