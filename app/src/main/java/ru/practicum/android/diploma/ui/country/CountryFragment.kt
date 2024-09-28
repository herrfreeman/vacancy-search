package ru.practicum.android.diploma.ui.country

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentCountryBinding
import ru.practicum.android.diploma.domain.models.Area
import ru.practicum.android.diploma.presentation.country.CountryState
import ru.practicum.android.diploma.presentation.country.CountryViewModel
import ru.practicum.android.diploma.ui.adapters.AreaAdapter

class CountryFragment : Fragment() {

    private var _binding: FragmentCountryBinding? = null
    private val binding
        get() = _binding!!

    private val viewModel by viewModel<CountryViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCountryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.getCountryLiveData().observe(viewLifecycleOwner) {
            when (it) {
                is CountryState.Content -> {
                    prepareCountriesList(it.countriesList)
                }

                is CountryState.Empty -> {
                    showListIsEmptyError()
                }

                is CountryState.Error -> {
                    showServerError()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadCountriesList()
    }

    private fun prepareCountriesList(countriesList: List<Area>) {
        binding.countriesList.isVisible = true
        binding.errorImage.isVisible = false
        binding.errorText.isVisible = false

        val adapter = AreaAdapter {
            viewModel.setCountry(it)
            findNavController().popBackStack()
        }

        adapter.setItems(countriesList)

        binding.countriesList.adapter = adapter
    }

    private fun showListIsEmptyError() {
        binding.countriesList.isVisible = false
        binding.errorImage.isVisible = true
        binding.errorText.isVisible = true

        binding.errorImage.setImageResource(R.drawable.empty_areas_list)
        binding.errorText.text = getString(R.string.unable_to_get_list)
    }

    private fun showServerError() {
        binding.countriesList.isVisible = false
        binding.errorImage.isVisible = true
        binding.errorText.isVisible = true

        binding.errorImage.setImageResource(R.drawable.empty_areas_list)
        binding.errorText.text = getString(R.string.unable_to_get_list)
    }
}
