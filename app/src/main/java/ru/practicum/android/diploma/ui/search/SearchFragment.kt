package ru.practicum.android.diploma.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentSearchBinding
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.presentation.search.SearchViewModel
import ru.practicum.android.diploma.ui.adapters.SearchVacancyAdapter
import ru.practicum.android.diploma.ui.vacancy.VacancyDetailsFragment
import ru.practicum.android.diploma.util.OnItemClickListener
import ru.practicum.android.diploma.util.debounce

class SearchFragment : Fragment() {

    private var onItemClickListener: OnItemClickListener? = null
    private var onVacancyClickDebounce: (Vacancy) -> Unit = {}
    private var vacancyAdapter: SearchVacancyAdapter? = null
    private var _binding: FragmentSearchBinding? = null
    private val binding
        get() = _binding!!
    private var textWatcher: TextWatcher? = null
    private val viewModel by viewModel<SearchViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.observeState().observe(viewLifecycleOwner) {
            render(it)
        }
        viewModel.observeShowToast().observe(viewLifecycleOwner) { toast ->
            showToast(toast)
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.filter -> {
                    findNavController().navigate(R.id.action_search_fragment_to_filter_fragment)
                    true
                }

                else -> false
            }
        }

        defaultState()
        onVacancyClickDebounce = debounce(
            CLICK_DEBOUNCE_DELAY, viewLifecycleOwner.lifecycleScope, false
        ) { vacancy ->
            launchVacancyDetails(vacancy)
        }
        onItemClickListener = OnItemClickListener { vacancy ->
            onVacancyClickDebounce(vacancy)
        }
        binding.topAppBar.menu.findItem(R.id.filter).setOnMenuItemClickListener { _ ->
            launchFilter()
        }

        vacancyAdapter = SearchVacancyAdapter(onItemClickListener!!)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = vacancyAdapter
        binding.recyclerView.animation = null

        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                return
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                return
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.searchDebounce(changedText = s.toString())
                if (s?.isNotEmpty() == true) {
                    binding.searchFrame.setEndIconDrawable(R.drawable.close_icon)
                } else {
                    viewModel.clearSearch()
                    binding.searchFrame.setEndIconDrawable(R.drawable.search_icon)
                }
            }
        }
        binding.inputEditText.addTextChangedListener(textWatcher!!)

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0) {
                    val pos = (binding.recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                    val itemsCount = vacancyAdapter?.itemCount
                    itemsCount?.let {
                        if (pos >= it - 2) {
                            viewModel.onLastItemReached()
                        }
                    }
                }
            }
        })

        binding.searchFrame.setEndIconOnClickListener {
            binding.inputEditText.setText(getString(R.string.empty_string))
            viewModel.clearSearch()
        }

    }

    private fun launchVacancyDetails(vacancy: Vacancy) {
        findNavController().navigate(
            R.id.action_search_fragment_to_vacancy_details_fragment,
            VacancyDetailsFragment.createArgs(vacancy = vacancy, vacancyNeedUpdate = true)
        )
    }

    private fun launchFilter(): Boolean {
        findNavController().navigate(
            R.id.action_search_fragment_to_filter_fragment,
        )
        return true
    }

    private fun render(state: SearchState) {
        when (state) {
            is SearchState.Content -> state.countOfVacancies?.let { showContent(state.vacanciesList, it) }
            is SearchState.Empty -> showEmpty(state.message)
            is SearchState.ServerError -> showError(state.errorMessage)
            is SearchState.LoadingNewExpression -> showLoading()
            is SearchState.InternetNotAvailable -> showLooseInternetConnection(state.errorMessage)
            is SearchState.Default -> defaultState()
            is SearchState.NextPageLoading -> vacancyAdapter?.showLoading(true)
        }
        showIconFilterIsOn(viewModel.hasFilter())
    }

    private fun showIconFilterIsOn(filtered: Boolean) {
        if (filtered) {
            binding.topAppBar.menu.findItem(R.id.filter).setIcon(R.drawable.filter_on_icon)
        } else {
            binding.topAppBar.menu.findItem(R.id.filter).setIcon(R.drawable.filter_off_icon)
        }
    }

    private fun showLooseInternetConnection(errorMessage: String) {
        showImageAndTextState()
        binding.vacancyCountTextView.isVisible = false
        binding.centralImageHolder.setImageResource(R.drawable.no_internet_image)
        binding.stateTextView.text = errorMessage
    }

    private fun showLoading() {
        binding.vacancyCountTextView.isVisible = false
        binding.progressBar.isVisible = true
        binding.centralImageHolder.isVisible = false
        binding.recyclerView.isVisible = false
        binding.stateTextView.isVisible = false
    }

    private fun showError(errorMessage: String) {
        showImageAndTextState()
        binding.vacancyCountTextView.isVisible = false
        binding.centralImageHolder.setImageResource(R.drawable.error_image)
        binding.stateTextView.text = errorMessage
    }

    private fun defaultState() {
        binding.vacancyCountTextView.isVisible = false
        binding.progressBar.isVisible = false
        binding.centralImageHolder.isVisible = true
        binding.recyclerView.isVisible = false
        binding.stateTextView.isVisible = false
        binding.centralImageHolder.setImageResource(R.drawable.empty_search_image)
    }

    private fun showEmpty(message: String) {
        showImageAndTextState()
        binding.vacancyCountTextView.isVisible = true
        binding.vacancyCountTextView.text = getString(R.string.no_vacancies)
        binding.centralImageHolder.setImageResource(R.drawable.empty_list_image)
        binding.stateTextView.text = message
    }

    private fun showImageAndTextState() {
        binding.progressBar.isVisible = false
        binding.centralImageHolder.isVisible = true
        binding.recyclerView.isVisible = false
        binding.stateTextView.isVisible = true
    }

    private fun showToast(additionalMessage: String) {
        Toast.makeText(requireContext(), additionalMessage, Toast.LENGTH_LONG).show()
    }

    private fun showContent(vacanciesList: List<Vacancy>, countOfVacancies: Int) {
        binding.progressBar.isVisible = false
        binding.centralImageHolder.isVisible = false
        binding.recyclerView.isVisible = true
        binding.stateTextView.isVisible = false
        binding.vacancyCountTextView.isVisible = true
        binding.vacancyCountTextView.text = buildString {
            append(getString(R.string.Founded))
            append(getString(R.string.empty_pace))
            append(
                context?.resources?.getQuantityString(
                    R.plurals.count_of_vacancies, countOfVacancies, countOfVacancies
                )
            )
        }
        vacancyAdapter?.showLoading(false)
        vacancyAdapter?.setData(vacanciesList)
        showIconFilterIsOn(viewModel.filterNotEmpty())

    }

    override fun onDestroyView() {
        super.onDestroyView()
        textWatcher?.let { binding.inputEditText.removeTextChangedListener(it) }
        _binding = null
    }

    companion object {
        const val CLICK_DEBOUNCE_DELAY = 300L
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkFilters()
        showIconFilterIsOn(viewModel.hasFilter())
    }
}
