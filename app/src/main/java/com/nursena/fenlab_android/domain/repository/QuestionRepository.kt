package com.nursena.fenlab_android.domain.repository

import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.data.remote.dto.request.AnswerCreateRequest
import com.nursena.fenlab_android.data.remote.dto.request.QuestionCreateRequest
import com.nursena.fenlab_android.domain.model.PaginatedData
import com.nursena.fenlab_android.domain.model.Question

interface QuestionRepository {

    suspend fun getExperimentQuestions(
        experimentId: Long,
        page: Int = 0,
        size: Int = 10
    ): ApiResult<PaginatedData<Question>>

    suspend fun askQuestion(experimentId: Long, request: QuestionCreateRequest): ApiResult<Question>

    suspend fun answerQuestion(questionId: Long, request: AnswerCreateRequest): ApiResult<Question>

    suspend fun deleteQuestion(questionId: Long): ApiResult<Unit>

    suspend fun getUnansweredQuestions(page: Int = 0, size: Int = 10): ApiResult<PaginatedData<Question>>
}
