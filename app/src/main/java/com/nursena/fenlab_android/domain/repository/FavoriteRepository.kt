package com.nursena.fenlab_android.domain.repository

import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.domain.model.Experiment
import com.nursena.fenlab_android.domain.model.PaginatedData

interface FavoriteRepository {

    suspend fun getUserFavorites(page: Int = 0, size: Int = 10): ApiResult<PaginatedData<Experiment>>

    suspend fun addToFavorites(experimentId: Long): ApiResult<String>

    suspend fun removeFromFavorites(experimentId: Long): ApiResult<String>

    suspend fun isFavorited(experimentId: Long): ApiResult<Boolean>
}
