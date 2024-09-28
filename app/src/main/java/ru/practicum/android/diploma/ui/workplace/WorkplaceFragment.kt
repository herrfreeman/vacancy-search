package ru.practicum.android.diploma.ui.workplace

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentWorkplaceBinding
import ru.practicum.android.diploma.presentation.workplace.WorkplaceState
import ru.practicum.android.diploma.presentation.workplace.WorkplaceViewModel

class WorkplaceFragment : Fragment() {

    private var _binding: FragmentWorkplaceBinding? = null
    private val binding
        get() = _binding!!

    private val workplaceViewModel by viewModel<WorkplaceViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWorkplaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.countryBar.setOnClickListener {
            findNavController().navigate(R.id.action_workplace_fragment_to_country_fragment)
        }

        binding.regionBar.setOnClickListener {
            findNavController().navigate(R.id.action_workplace_fragment_to_region_fragment)
        }

        binding.chooseButton.setOnClickListener {
            workplaceViewModel.setSelectedArea()
            findNavController().popBackStack()
        }
        setCountryListener()
        setRegionListener()
        workplaceViewModel.getWorkplaceStateLiveData().observe(viewLifecycleOwner) {
            when (it) {
                is WorkplaceState.CountryIsPicked -> {
                    showCountry(it.country.name)
                    hideRegion()
                }

                is WorkplaceState.CountryAndRegionIsPicked -> {
                    showCountry(it.country.name)
                    showRegion(it.city.name)
                }

                is WorkplaceState.NothingIsPicked -> {
                    hideCountry()
                    hideRegion()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showCountry(country: String) {
        binding.countryBar.setEndIconDrawable(R.drawable.close_icon)
        binding.countryBar.defaultHintTextColor = setHintOnValueColor()
        binding.countryBar.setEndIconOnClickListener {
            binding.chooseButton.isVisible = true
            workplaceViewModel.deleteCountry()
            binding.countryName.text = null
            binding.countryBar.defaultHintTextColor = setHintGrayColor()
            binding.countryBar.setEndIconDrawable(R.drawable.arrow_forward)
            binding.countryBar.setEndIconOnClickListener {
                findNavController().navigate(R.id.action_workplace_fragment_to_country_fragment)
            }
        }
        binding.countryName.setText(country)
    }

    private fun hideCountry() {
        binding.countryName.text = null
        binding.countryBar.defaultHintTextColor = setHintGrayColor()
        binding.countryBar.setEndIconDrawable(R.drawable.arrow_forward)
        binding.countryBar.setEndIconOnClickListener {
            findNavController().navigate(R.id.action_workplace_fragment_to_country_fragment)
        }
    }

    private fun showRegion(region: String) {
        binding.regionBar.setEndIconDrawable(R.drawable.close_icon)
        binding.regionBar.defaultHintTextColor = setHintOnValueColor()
        binding.regionBar.setEndIconOnClickListener {
            binding.chooseButton.isVisible = true
            workplaceViewModel.deleteRegion()
            binding.regionName.text = null
            binding.regionBar.defaultHintTextColor = setHintGrayColor()
            binding.regionBar.setEndIconDrawable(R.drawable.arrow_forward)
            binding.regionBar.setEndIconOnClickListener {
                findNavController().navigate(R.id.action_workplace_fragment_to_region_fragment)
            }
        }
        binding.regionName.setText(region)
    }

    private fun hideRegion() {
        binding.regionName.text = null
        binding.regionBar.defaultHintTextColor = setHintGrayColor()
        binding.regionBar.setEndIconDrawable(R.drawable.arrow_forward)
        binding.regionBar.setEndIconOnClickListener {
            findNavController().navigate(R.id.action_workplace_fragment_to_region_fragment)
        }
    }

    private fun setHintOnValueColor(): ColorStateList {
        val blackOrWhiteColor = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_enabled)),
            intArrayOf(ContextCompat.getColor(requireContext(), R.color.text_hint_country_industry))
        )
        return blackOrWhiteColor
    }

    private fun setHintGrayColor(): ColorStateList {
        val grayColor = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_enabled)),
            intArrayOf(ContextCompat.getColor(requireContext(), R.color.gray))
        )
        return grayColor
    }

    private fun setRegionListener() {
        binding.regionName.setOnClickListener {
            findNavController().navigate(R.id.action_workplace_fragment_to_region_fragment)
        }

        binding.regionBar.setOnClickListener {
            findNavController().navigate(R.id.action_workplace_fragment_to_region_fragment)
        }

        binding.regionBar.setEndIconOnClickListener {
            findNavController().navigate(R.id.action_workplace_fragment_to_region_fragment)
        }
    }

    private fun showChooseButton() {
        binding.chooseButton.isVisible = !workplaceViewModel.checkIfFilterIsSaved()
    }

    private fun setCountryListener() {
        binding.countryName.setOnClickListener {
            findNavController().navigate(R.id.action_workplace_fragment_to_country_fragment)
        }
        binding.countryBar.setOnClickListener {
            findNavController().navigate(R.id.action_workplace_fragment_to_country_fragment)
        }
        binding.countryBar.setEndIconOnClickListener {
            findNavController().navigate(R.id.action_workplace_fragment_to_country_fragment)
        }
    }

    override fun onResume() {
        super.onResume()
        workplaceViewModel.loadFilter()
        showChooseButton()
    }
}
