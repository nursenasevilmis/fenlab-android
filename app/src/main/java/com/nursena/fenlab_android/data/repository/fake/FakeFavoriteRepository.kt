package com.nursena.fenlab_android.data.repository.fake

import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.domain.model.Experiment
import com.nursena.fenlab_android.domain.model.PaginatedData
import com.nursena.fenlab_android.domain.repository.FavoriteRepository
import com.nursena.fenlab_android.ui.preview.MockData
import kotlinx.coroutines.delay
import javax.inject.Inject

class FakeFavoriteRepository @Inject constructor() : FavoriteRepository {

    private val favoritedIds = MockData.mockExperiments
        .filter { it.isFavoritedByCurrentUser }
        .map { it.id }
        .toMutableSet()

    override suspend fun getUserFavorites(page: Int, size: Int): ApiResult<PaginatedData<Experiment>> {
        delay(400)
        val favs = MockData.mockExperiments.filter { it.id in favoritedIds }
        return ApiResult.Success(PaginatedData(favs, 0, favs.size, favs.size.toLong(), 1, true, true))
    }

    override suspend fun addToFavorites(experimentId: Long): ApiResult<String> {
        favoritedIds.add(experimentId)
        return ApiResult.Success("Favorilere eklendi.")
    }

    override suspend fun removeFromFavorites(experimentId: Long): ApiResult<String> {
        favoritedIds.remove(experimentId)
        return ApiResult.Success("Favorilerden çıkarıldı.")
    }

    override suspend fun isFavorited(experimentId: Long): ApiResult<Boolean> =
        ApiResult.Success(experimentId in favoritedIds)
}