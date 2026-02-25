package com.nursena.fenlab_android.domain.repository

import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.data.remote.dto.request.RatingRequest
import com.nursena.fenlab_android.domain.model.Rating

interface RatingRepository {

    suspend fun rateExperiment(experimentId: Long, request: RatingRequest): ApiResult<Rating>

    suspend fun getUserRating(experimentId: Long): ApiResult<Rating?>

    suspend fun getAverageRating(experimentId: Long): ApiResult<Double?>
}
