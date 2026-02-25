package com.nursena.fenlab_android.data.remote.api

import com.nursena.fenlab_android.data.remote.dto.request.RatingRequest
import com.nursena.fenlab_android.data.remote.dto.response.RatingResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface RatingApi {

    @POST("api/ratings/experiment/{experimentId}")
    suspend fun rateExperiment(
        @Path("experimentId") experimentId: Long,
        @Body request: RatingRequest
    ): RatingResponse

    @GET("api/ratings/experiment/{experimentId}/me")
    suspend fun getUserRating(
        @Path("experimentId") experimentId: Long
    ): RatingResponse?

    @GET("api/ratings/experiment/{experimentId}/average")
    suspend fun getAverageRating(
        @Path("experimentId") experimentId: Long
    ): Double?                      // nullable — hiç puan yoksa null döner
}
