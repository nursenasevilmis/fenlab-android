package com.nursena.fenlab_android.data.remote.api

import com.nursena.fenlab_android.data.remote.dto.response.ExperimentSummaryResponse
import com.nursena.fenlab_android.data.remote.dto.response.PaginatedResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FavoriteApi {
    @GET("api/favorites")
    suspend fun getUserFavorites(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): PaginatedResponse<ExperimentSummaryResponse>

    @POST("api/favorites/{experimentId}")
    suspend fun addToFavorites(@Path("experimentId") experimentId: Long): Map<String, String>

    @DELETE("api/favorites/{experimentId}")
    suspend fun removeFromFavorites(@Path("experimentId") experimentId: Long): Map<String, String>

    @GET("api/favorites/{experimentId}/is-favorited")
    suspend fun isFavorited(@Path("experimentId") experimentId: Long): Map<String, Boolean>
}
