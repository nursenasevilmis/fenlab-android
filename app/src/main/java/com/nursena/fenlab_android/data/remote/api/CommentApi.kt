package com.nursena.fenlab_android.data.remote.api

import com.nursena.fenlab_android.data.remote.dto.request.CommentCreateRequest
import com.nursena.fenlab_android.data.remote.dto.request.CommentUpdateRequest
import com.nursena.fenlab_android.data.remote.dto.response.CommentResponse
import com.nursena.fenlab_android.data.remote.dto.response.PaginatedResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface CommentApi {

    @GET("api/comments/experiment/{experimentId}")
    suspend fun getExperimentComments(
        @Path("experimentId") experimentId: Long,
        @Query("page")        page: Int = 0,
        @Query("size")        size: Int = 10
    ): PaginatedResponse<CommentResponse>

    @POST("api/comments/experiment/{experimentId}")
    suspend fun addComment(
        @Path("experimentId") experimentId: Long,
        @Body request: CommentCreateRequest
    ): CommentResponse

    @PUT("api/comments/{commentId}")
    suspend fun updateComment(
        @Path("commentId") commentId: Long,
        @Body request: CommentUpdateRequest
    ): CommentResponse

    @DELETE("api/comments/{commentId}")
    suspend fun deleteComment(
        @Path("commentId") commentId: Long
    )
}