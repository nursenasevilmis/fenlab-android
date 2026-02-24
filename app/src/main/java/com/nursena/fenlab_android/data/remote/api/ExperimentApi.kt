package com.nursena.fenlab_android.data.remote.api

import com.nursena.fenlab_android.data.remote.dto.request.ExperimentCreateRequest
import com.nursena.fenlab_android.data.remote.dto.request.ExperimentUpdateRequest
import com.nursena.fenlab_android.data.remote.dto.response.ExperimentDetailResponse
import com.nursena.fenlab_android.data.remote.dto.response.ExperimentSummaryResponse
import com.nursena.fenlab_android.data.remote.dto.response.PaginatedResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ExperimentApi {
    @GET("api/experiments")
    suspend fun getAllExperiments(
        @Query("search") search: String? = null,
        @Query("subject") subject: String? = null,
        @Query("environment") environment: String? = null,
        @Query("gradeLevel") gradeLevel: Int? = null,
        @Query("difficulty") difficulty: String? = null,
        @Query("sortType") sortType: String = "MOST_RECENT",
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PaginatedResponse<ExperimentSummaryResponse>

    @GET("api/experiments/{id}")
    suspend fun getExperimentById(@Path("id") id: Long): ExperimentDetailResponse

    @POST("api/experiments")
    suspend fun createExperiment(@Body request: ExperimentCreateRequest): ExperimentDetailResponse

    @PUT("api/experiments/{id}")
    suspend fun updateExperiment(
        @Path("id") id: Long,
        @Body request: ExperimentUpdateRequest
    ): ExperimentDetailResponse

    @DELETE("api/experiments/{id}")
    suspend fun deleteExperiment(@Path("id") id: Long)

    @GET("api/experiments/user/{userId}")
    suspend fun getUserExperiments(
        @Path("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PaginatedResponse<ExperimentSummaryResponse>

    @GET("api/experiments/subjects")
    suspend fun getAllSubjects(): List<String>
}