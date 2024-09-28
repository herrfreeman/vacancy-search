package ru.practicum.android.diploma.data.network

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.QueryMap
import ru.practicum.android.diploma.BuildConfig
import ru.practicum.android.diploma.data.dto.AreaDto
import ru.practicum.android.diploma.data.dto.CountryDto
import ru.practicum.android.diploma.data.dto.DictionariesDto
import ru.practicum.android.diploma.data.dto.IndustryDto
import ru.practicum.android.diploma.data.dto.VacancyFullDto

interface HHApi {

    @Headers(
        "Authorization: Bearer ${BuildConfig.HH_ACCESS_TOKEN}",
        "HH-User-Agent: FindNewJob/1.0 (freeman@blackmesa.ru)"
    )
    @GET("/vacancies")
    suspend fun getVacancies(@QueryMap options: Map<String, String>): VacanciesSearchResponse

    @Headers(
        "Authorization: Bearer ${BuildConfig.HH_ACCESS_TOKEN}",
        "HH-User-Agent: FindNewJob/1.0 (freeman@blackmesa.ru)"
    )
    @GET("/industries")
    suspend fun getIndustries(): List<IndustryDto>

    @Headers(
        "Authorization: Bearer ${BuildConfig.HH_ACCESS_TOKEN}",
        "HH-User-Agent: FindNewJob/1.0 (freeman@blackmesa.ru)"
    )
    @GET("/areas/countries")
    suspend fun getCountries(): List<CountryDto>

    @GET("/areas")
    suspend fun getAreas(): List<AreaDto>

    @GET("/areas/{areaId}")
    suspend fun getAreasById(
        @Path("areaId") areaId: String
    ): AreaDto

    @Headers(
        "Authorization: Bearer ${BuildConfig.HH_ACCESS_TOKEN}",
        "HH-User-Agent: FindNewJob/1.0 (freeman@blackmesa.ru)"
    )
    @GET("/vacancies/{vacancyId}")
    suspend fun getVacancyFull(
        @Path("vacancyId") vacancyId: String
    ): VacancyFullDto

    @Headers(
        "Authorization: Bearer ${BuildConfig.HH_ACCESS_TOKEN}",
        "HH-User-Agent: FindNewJob/1.0 (freeman@blackmesa.ru)"
    )
    @GET("/dictionaries")
    suspend fun getDictionaries(): DictionariesDto
}
