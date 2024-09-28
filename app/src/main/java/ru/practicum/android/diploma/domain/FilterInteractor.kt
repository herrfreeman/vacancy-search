package ru.practicum.android.diploma.domain

import ru.practicum.android.diploma.domain.models.Area
import ru.practicum.android.diploma.domain.models.Filter
import ru.practicum.android.diploma.domain.models.Industry

interface FilterInteractor {
    fun currentFilter(): Filter
    fun appliedFilter(): Filter
    fun selectCountry(country: Area?)
    fun selectRegion(region: Area?)
    fun selectedCountry(): Area?
    fun selectedRegion(): Area?
    fun setCountry(country: Area?)
    fun setArea(area: Area?)
    fun setIndustry(industry: Industry?)
    fun setSalary(salary: String?)
    fun setOnlyWithSalary(onlyWithSalary: Boolean)
    fun apply()
    fun flushCurrentFilter()
    fun checkRegionsAreSaved(): Boolean
}
