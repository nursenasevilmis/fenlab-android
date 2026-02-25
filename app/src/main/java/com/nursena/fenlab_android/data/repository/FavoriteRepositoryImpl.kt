package com.nursena.fenlab_android.data.repository

import com.nursena.fenlab_android.core.base.BaseRepository
import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.data.remote.api.FavoriteApi
import com.nursena.fenlab_android.data.remote.mapper.toDomain
import com.nursena.fenlab_android.domain.model.Experiment
import com.nursena.fenlab_android.domain.model.PaginatedData
import com.nursena.fenlab_android.domain.repository.FavoriteRepository
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteApi: FavoriteApi
) : BaseRepository(), FavoriteRepository {

    override suspend fun getUserFavorites(page: Int, size: Int): ApiResult<PaginatedData<Experiment>> = safeApiCall {
        favoriteApi.getUserFavorites(page, size).toDomain { it.toDomain() }
    }

    override suspend fun addToFavorites(experimentId: Long): ApiResult<String> = safeApiCall {
        favoriteApi.addToFavorites(experimentId)["message"] ?: ""
    }

    override suspend fun removeFromFavorites(experimentId: Long): ApiResult<String> = safeApiCall {
        favoriteApi.removeFromFavorites(experimentId)["message"] ?: ""
    }

    override suspend fun isFavorited(experimentId: Long): ApiResult<Boolean> = safeApiCall {
        favoriteApi.isFavorited(experimentId)["isFavorited"] ?: false
    }
}
