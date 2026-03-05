package com.nursena.fenlab_android.data.repository

import com.nursena.fenlab_android.core.base.BaseRepository
import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.data.remote.api.ExperimentApi
import com.nursena.fenlab_android.data.remote.dto.request.ExperimentCreateRequest
import com.nursena.fenlab_android.data.remote.dto.request.ExperimentUpdateRequest
import com.nursena.fenlab_android.data.remote.mapper.toDomain
import com.nursena.fenlab_android.domain.model.Experiment
import com.nursena.fenlab_android.domain.model.ExperimentDetail
import com.nursena.fenlab_android.domain.model.PaginatedData
import com.nursena.fenlab_android.domain.repository.ExperimentRepository
import javax.inject.Inject

class ExperimentRepositoryImpl @Inject constructor(
    private val experimentApi: ExperimentApi
) : BaseRepository(), ExperimentRepository {

    override suspend fun getAllExperiments(
        search: String?,
        subject: String?,
        environment: String?,
        minGradeLevel: Int?,
        maxGradeLevel: Int?,
        difficulty: String?,
        sortType: String,
        page: Int,
        size: Int
    ): ApiResult<PaginatedData<Experiment>> = safeApiCall {
        experimentApi.getAllExperiments(
            search        = search,
            subject       = subject,
            environment   = environment,
            minGradeLevel = minGradeLevel,
            maxGradeLevel = maxGradeLevel,
            difficulty    = difficulty,
            sortType      = sortType,
            page          = page,
            size          = size
        ).toDomain { it.toDomain() }
    }

    override suspend fun getExperimentById(experimentId: Long): ApiResult<ExperimentDetail> = safeApiCall {
        experimentApi.getExperimentById(experimentId).toDomain()
    }

    override suspend fun createExperiment(request: ExperimentCreateRequest): ApiResult<ExperimentDetail> = safeApiCall {
        experimentApi.createExperiment(request).toDomain()
    }

    override suspend fun updateExperiment(experimentId: Long, request: ExperimentUpdateRequest): ApiResult<ExperimentDetail> = safeApiCall {
        experimentApi.updateExperiment(experimentId, request).toDomain()
    }

    override suspend fun deleteExperiment(experimentId: Long): ApiResult<Unit> = safeApiCall {
        experimentApi.deleteExperiment(experimentId)
    }

    override suspend fun getUserExperiments(userId: Long, page: Int, size: Int): ApiResult<PaginatedData<Experiment>> = safeApiCall {
        experimentApi.getUserExperiments(userId, page, size).toDomain { it.toDomain() }
    }

    override suspend fun getAllSubjects(): ApiResult<List<String>> = safeApiCall {
        experimentApi.getAllSubjects()
    }
}