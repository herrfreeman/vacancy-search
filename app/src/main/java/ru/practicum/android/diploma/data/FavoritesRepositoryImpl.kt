package ru.practicum.android.diploma.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import ru.practicum.android.diploma.data.db.AppDatabase
import ru.practicum.android.diploma.domain.api.FavoritesRepository
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.util.ErrorType
import ru.practicum.android.diploma.util.Resource
import ru.practicum.android.diploma.util.toEntity
import ru.practicum.android.diploma.util.toVacancy
import java.sql.SQLException

class FavoritesRepositoryImpl(
    private val database: AppDatabase,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : FavoritesRepository {
    override fun getFavoriteVacancies(): Flow<Resource<List<Vacancy>>> = flow {
        try {
            emit(
                Resource.Success(
                    withContext(defaultDispatcher) {
                        database.favoriteVacancyDao().getFavoriteVacancies().map {
                            it.toVacancy()
                        }
                    }
                )
            )
        } catch (e: SQLException) {
            emit(
                Resource.Error(
                    errorType = ErrorType.SQLError,
                    message = e.toString()
                )
            )
        }
    }

    override suspend fun addVacancyToFavorites(vacancy: Vacancy) {
        withContext(defaultDispatcher) {
            database.favoriteVacancyDao().insertVacancy(vacancy.toEntity())
        }
    }

    override suspend fun deleteVacancyFromFavorites(vacancy: Vacancy) {
        withContext(defaultDispatcher) {
            database.favoriteVacancyDao().deleteFromFavorite(vacancy.toEntity())
        }
    }

    override suspend fun checkVacancyIsFavorite(vacancy: Vacancy): Boolean {
        return withContext(defaultDispatcher) {
            database.favoriteVacancyDao().findVacancyById(vacancy.id).isNotEmpty()
        }
    }
}
