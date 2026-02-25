package com.nursena.fenlab_android.data.repository

import com.nursena.fenlab_android.core.base.BaseRepository
import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.data.remote.api.QuestionApi
import com.nursena.fenlab_android.data.remote.dto.request.AnswerCreateRequest
import com.nursena.fenlab_android.data.remote.dto.request.QuestionCreateRequest
import com.nursena.fenlab_android.data.remote.mapper.toDomain
import com.nursena.fenlab_android.domain.model.PaginatedData
import com.nursena.fenlab_android.domain.model.Question
import com.nursena.fenlab_android.domain.repository.QuestionRepository
import javax.inject.Inject

class QuestionRepositoryImpl @Inject constructor(
    private val questionApi: QuestionApi
) : BaseRepository(), QuestionRepository {

    override suspend fun getExperimentQuestions(
        experimentId: Long,
        page: Int,
        size: Int
    ): ApiResult<PaginatedData<Question>> = safeApiCall {
        questionApi.getExperimentQuestions(experimentId, page, size).toDomain { it.toDomain() }
    }

    override suspend fun askQuestion(experimentId: Long, request: QuestionCreateRequest): ApiResult<Question> = safeApiCall {
        questionApi.askQuestion(experimentId, request).toDomain()
    }

    override suspend fun answerQuestion(questionId: Long, request: AnswerCreateRequest): ApiResult<Question> = safeApiCall {
        questionApi.answerQuestion(questionId, request).toDomain()
    }

    override suspend fun deleteQuestion(questionId: Long): ApiResult<Unit> = safeApiCall {
        questionApi.deleteQuestion(questionId)
    }

    override suspend fun getUnansweredQuestions(page: Int, size: Int): ApiResult<PaginatedData<Question>> = safeApiCall {
        questionApi.getUnansweredQuestions(page, size).toDomain { it.toDomain() }
    }
}
