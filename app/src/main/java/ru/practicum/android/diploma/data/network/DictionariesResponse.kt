package ru.practicum.android.diploma.data.network

import ru.practicum.android.diploma.data.dto.DictionariesDto

data class DictionariesResponse(
    val dictionaries: DictionariesDto
) : Response()
