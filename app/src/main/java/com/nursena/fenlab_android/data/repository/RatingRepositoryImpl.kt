package com.nursena.fenlab_android.data.repository

import com.nursena.fenlab_android.core.base.BaseRepository
import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.data.remote.api.RatingApi
import com.nursena.fenlab_android.data.remote.dto.request.RatingRequest
import com.nursena.fenlab_android.data.remote.mapper.toDomain
import com.nursena.fenlab_android.domain.model.Rating
import com.nursena.fenlab_android.domain.repository.RatingRepository
import javax.inject.Inject

class RatingRepositoryImpl @Inject constructor(
    private val ratingApi: RatingApi
) : BaseRepository(), RatingRepository {

    override suspend fun rateExperiment(experimentId: Long, request: RatingRequest): ApiResult<Rating> = safeApiCall {
        ratingApi.rateExperiment(experimentId, request).toDomain()
    }

    override suspend fun getUserRating(experimentId: Long): ApiResult<Rating?> = safeApiCall {
        ratingApi.getUserRating(experimentId)?.toDomain()
    }

    override suspend fun getAverageRating(experimentId: Long): ApiResult<Double?> = safeApiCall {
        ratingApi.getAverageRating(experimentId)
    }
}
