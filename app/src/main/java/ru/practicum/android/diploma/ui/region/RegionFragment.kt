package ru.practicum.android.diploma.ui.region

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentRegionBinding
import ru.practicum.android.diploma.domain.models.Area
import ru.practicum.android.diploma.presentation.region.RegionState
import ru.practicum.android.diploma.presentation.region.RegionViewModel
import ru.practicum.android.diploma.presentation.workplace.WorkplaceState
import ru.practicum.android.diploma.ui.adapters.AreaAdapter

class RegionFragment : Fragment() {

    private var _binding: FragmentRegionBinding? = null
    private val binding
        get() = _binding!!

    private val viewModel by viewModel<RegionViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.getFilterState().observe(viewLifecycleOwner) {
            when (it) {
                is WorkplaceState.CountryAndRegionIsPicked -> {
                    viewModel.loadRegionsList(it.country.id)
                }
                is WorkplaceState.CountryIsPicked -> {
                    viewModel.loadRegionsList(it.country.id)
                }
                WorkplaceState.NothingIsPicked -> {
                    viewModel.loadRegionsList(null)
                }
            }
        }

        viewModel.getRegionLiveData().observe(viewLifecycleOwner) {
            when (it) {
                is RegionState.Content -> {
                    prepareRegionsList(it.regionsList)
                }

                RegionState.Empty -> {
                    showListIsEmptyError()
                }

                RegionState.Error -> {
                    showServerError()
                }

                RegionState.NoRegion -> {
                    showNoRegion()
                }
            }
        }

        binding.inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                return
            }

            override fun onTextChanged(searchQuery: CharSequence?, p1: Int, p2: Int, p3: Int) {
                when (val areaFilterState = viewModel.getFilterState().value) {
                    is WorkplaceState.NothingIsPicked -> {
                        viewModel.filter(searchQuery.toString(), null)
                    }

                    is WorkplaceState.CountryAndRegionIsPicked -> {
                        viewModel.filter(searchQuery.toString(), areaFilterState.country.id)
                    }

                    is WorkplaceState.CountryIsPicked -> {
                        viewModel.filter(searchQuery.toString(), areaFilterState.country.id)
                    }

                    else -> {
                        viewModel.filter(searchQuery.toString(), null)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    binding.searchFrame.setEndIconDrawable(R.drawable.close_icon)
                } else {
                    binding.searchFrame.setEndIconDrawable(R.drawable.search_icon)
                }
            }

        })

        binding.searchFrame.setEndIconOnClickListener {
            binding.inputEditText.setText(getString(R.string.empty_string))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.defineCurrentFilterState()
    }

    private fun prepareRegionsList(regionsList: List<Area>) {
        binding.regionsList.isVisible = true
        binding.errorImage.isVisible = false
        binding.errorText.isVisible = false

        val adapter = AreaAdapter {
            viewModel.setCountryAndRegion(it)
            findNavController().popBackStack()
        }

        adapter.setItems(regionsList)

        binding.regionsList.adapter = adapter
    }

    private fun showListIsEmptyError() {
        binding.regionsList.isVisible = false
        binding.errorImage.isVisible = true
        binding.errorText.isVisible = true

        binding.errorImage.setImageResource(R.drawable.empty_areas_list)
        binding.errorText.text = getString(R.string.unable_to_get_list)
    }

    private fun showServerError() {
        binding.regionsList.isVisible = false
        binding.errorImage.isVisible = true
        binding.errorText.isVisible = true

        binding.errorImage.setImageResource(R.drawable.empty_areas_list)
        binding.errorText.text = getString(R.string.unable_to_get_list)
    }

    private fun showNoRegion() {
        binding.regionsList.isVisible = false
        binding.errorImage.isVisible = true
        binding.errorText.isVisible = true

        binding.errorImage.setImageResource(R.drawable.empty_list_image)
        binding.errorText.text = getString(R.string.no_region)
    }
}
