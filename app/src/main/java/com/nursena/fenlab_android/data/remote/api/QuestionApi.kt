package com.nursena.fenlab_android.data.remote.api

import com.nursena.fenlab_android.data.remote.dto.request.AnswerCreateRequest
import com.nursena.fenlab_android.data.remote.dto.request.QuestionCreateRequest
import com.nursena.fenlab_android.data.remote.dto.response.PaginatedResponse
import com.nursena.fenlab_android.data.remote.dto.response.QuestionResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface QuestionApi {

    @GET("api/questions/experiment/{experimentId}")
    suspend fun getExperimentQuestions(
        @Path("experimentId") experimentId: Long,
        @Query("page")        page: Int = 0,
        @Query("size")        size: Int = 10
    ): PaginatedResponse<QuestionResponse>

    @POST("api/questions/experiment/{experimentId}")
    suspend fun askQuestion(
        @Path("experimentId") experimentId: Long,
        @Body request: QuestionCreateRequest
    ): QuestionResponse

    @POST("api/questions/{questionId}/answer")
    suspend fun answerQuestion(
        @Path("questionId") questionId: Long,
        @Body request: AnswerCreateRequest
    ): QuestionResponse

    @DELETE("api/questions/{questionId}")
    suspend fun deleteQuestion(
        @Path("questionId") questionId: Long
    )

    @GET("api/questions/unanswered")
    suspend fun getUnansweredQuestions(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): PaginatedResponse<QuestionResponse>
}