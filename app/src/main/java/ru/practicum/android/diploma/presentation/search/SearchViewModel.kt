package ru.practicum.android.diploma.presentation.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.FilterInteractor
import ru.practicum.android.diploma.domain.api.VacanciesInteractor
import ru.practicum.android.diploma.domain.models.Filter
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.ui.search.SearchState
import ru.practicum.android.diploma.util.ErrorType
import ru.practicum.android.diploma.util.SingleLiveEvent
import ru.practicum.android.diploma.util.debounce

class SearchViewModel(
    private val vacanciesInteractor: VacanciesInteractor,
    private val filterInteractor: FilterInteractor,
    application: Application
) : AndroidViewModel(application) {
    private var appliedFilter: Filter = filterInteractor.appliedFilter()
    private var isNextPageLoading: Boolean = false
    private var currentPage: Int = 0
    private var maxPage: Int? = null
    private var latestSearchText: String? = null
    private var vacanciesList = mutableListOf<Vacancy>()
    private val vacancySearchDebounce =
        debounce<String>(SEARCH_DEBOUNCE_DELAY, viewModelScope, true) { changedText ->
            clearSearch()
            searchVacancies(changedText)
        }
    private val showToast = SingleLiveEvent<String>()
    private val stateLiveData = MutableLiveData<SearchState>()
    fun observeState(): LiveData<SearchState> = stateLiveData
    fun observeShowToast(): LiveData<String> = showToast
    fun searchDebounce(changedText: String) {
        if (latestSearchText != changedText) {
            latestSearchText = changedText
            filterInteractor.apply()
            appliedFilter = filterInteractor.appliedFilter()
            vacancySearchDebounce(changedText)
        }
    }

    // Функция для пагинации
    private fun searchVacancies(searchText: String) {
        if (searchText.isNotBlank()) {
            if (this.currentPage == maxPage) {
                return
            } else {
                if (currentPage == 0) {
                    renderState(SearchState.LoadingNewExpression)
                } else {
                    isNextPageLoading = true
                    renderState(SearchState.NextPageLoading)
                }
                searchRequest(searchText, currentPage)
                currentPage += 1
            }
        } else {
            renderState(SearchState.Default)
        }
    }

    private fun searchRequest(searchText: String, currentPage: Int) {
        if (searchText.isNotEmpty()) {
            viewModelScope.launch {
                vacanciesInteractor.searchVacancies(
                    searchText,
                    appliedFilter,
                    currentPage,
                    PER_PAGE_SIZE
                )
                    .collect { resource ->
                        processResult(
                            resource.data?.vacancies,
                            resource.data?.found,
                            resource.errorType,
                            resource.message
                        )
                        maxPage = resource.data?.count
                    }
            }
        }
    }

    private fun processResult(
        foundVacancies: List<Vacancy>?,
        countOfVacancies: Int?,
        errorType: ErrorType?,
        errorMessage: String?
    ) {
        val messageServerError = getApplication<Application>().getString(R.string.server_error)
        val messageNoInternet = getApplication<Application>().getString(R.string.internet_is_not_available)
        val messageCheckConnection = getApplication<Application>().getString(R.string.check_connection_message)

        if (foundVacancies != null) {
            vacanciesList.addAll(foundVacancies)
        }
        when {
            errorType != null -> {
                if (errorType == ErrorType.NoConnection) {
                    if (isNextPageLoading) {
                        renderState(SearchState.Content(vacanciesList, null))
                    } else {
                        renderState(SearchState.InternetNotAvailable(messageNoInternet))
                    }

                    showToast(messageCheckConnection)
                } else {
                    if (isNextPageLoading) {
                        renderState(SearchState.Content(vacanciesList, null))
                    } else {
                        renderState(SearchState.ServerError(messageServerError))
                    }
                    showToast(errorMessage ?: messageServerError)
                }
                isNextPageLoading = false
            }

            vacanciesList.isEmpty() -> {
                renderState(
                    SearchState.Empty(
                        message = getApplication<Application>().getString(R.string.not_get_a_list_of_vacancies),
                    )
                )
            }

            else -> {
                renderState(
                    SearchState.Content(
                        vacanciesList.distinct(),
                        countOfVacancies,
                    )
                )
                isNextPageLoading = false
            }
        }
    }

    private fun renderState(state: SearchState) {
        stateLiveData.postValue(state)
    }

    private fun showToast(message: String) {
        showToast.postValue(message)
    }

    fun clearSearch() {
        renderState(SearchState.Default)
        currentPage = 0
        maxPage = null
        vacanciesList.clear()
    }

    fun onLastItemReached() {
        if (isNextPageLoading) {
            return
        } else {
            searchVacancies(latestSearchText!!)
        }
    }

    fun filterNotEmpty() = appliedFilter != Filter()
    fun checkFilters() {
        val newFilter = filterInteractor.appliedFilter()
        if (newFilter != appliedFilter) {
            appliedFilter = newFilter
            currentPage = 0
            vacanciesList.clear()
            latestSearchText?.let { searchText ->
                searchVacancies(searchText)
            }
        }
    }

    fun hasFilter() = filterInteractor.currentFilter() != Filter()

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val PER_PAGE_SIZE = 20
    }
}
