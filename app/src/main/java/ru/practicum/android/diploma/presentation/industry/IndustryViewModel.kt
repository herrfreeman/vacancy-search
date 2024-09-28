package ru.practicum.android.diploma.presentation.industry

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.FilterInteractor
import ru.practicum.android.diploma.domain.api.DictionariesInteractor
import ru.practicum.android.diploma.domain.models.Industry
import ru.practicum.android.diploma.ui.industry.IndustryState
import ru.practicum.android.diploma.util.debounce

class IndustryViewModel(
    val dictionariesInteractor: DictionariesInteractor,
    val filterInteractor: FilterInteractor,
    application: Application
) : AndroidViewModel(application) {

    private val stateLiveData = MutableLiveData<IndustryState>()
    private var latestSearchText: String? = null
    private var industriesList: List<Industry>? = null
    fun observeState(): LiveData<IndustryState> = stateLiveData

    init {
        loadIndustries()
    }

    private fun loadIndustries() {
        stateLiveData.postValue(IndustryState.Loading)
        viewModelScope.launch {
            dictionariesInteractor.getIndustries().collect {
                if (it.errorType != null) {
                    industriesList = null
                    stateLiveData.postValue(IndustryState.Error(it.errorType))
                } else if (it.data != null) {
                    industriesList = it.data
                    postFilteredIndustries(industriesList!!, latestSearchText)
                }
            }
        }
    }

    fun searchDebounce(changedText: String) {
        if (latestSearchText != changedText) {
            latestSearchText = changedText
            industrySearchDebounce(changedText)
        }
    }

    private val industrySearchDebounce =
        debounce<String>(SEARCH_DEBOUNCE_DELAY, viewModelScope, true) { changedText ->
            searchIndustries(changedText)
        }

    private fun searchIndustries(searchText: String) {
        if (industriesList == null) {
            loadIndustries()
        } else {
            postFilteredIndustries(industriesList!!, searchText)
        }
    }

    private fun postFilteredIndustries(industries: List<Industry>, filterString: String?) {
        var filtered = industries
        if (filterString != null) {
            filtered = filtered.filter {
                it.name.contains(filterString, ignoreCase = true)
            }
        }
        if (filtered.isEmpty()) {
            stateLiveData.postValue(
                IndustryState.Empty(
                    message = getApplication<Application>().getString(
                        R.string.industry_not_found
                    ),
                )
            )
        } else {
            stateLiveData.postValue(IndustryState.Content(filtered))
        }
    }

    fun setIndustryToFilter(industry: Industry?) {
        filterInteractor.setIndustry(industry)
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 500L
    }
}
