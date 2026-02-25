package com.nursena.fenlab_android.domain.repository

import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.data.remote.dto.request.ExperimentCreateRequest
import com.nursena.fenlab_android.data.remote.dto.request.ExperimentUpdateRequest
import com.nursena.fenlab_android.domain.model.Experiment
import com.nursena.fenlab_android.domain.model.ExperimentDetail
import com.nursena.fenlab_android.domain.model.PaginatedData

interface ExperimentRepository {

    suspend fun getAllExperiments(
        search: String?      = null,
        subject: String?     = null,
        environment: String? = null,
        gradeLevel: Int?     = null,
        difficulty: String?  = null,
        sortType: String     = "MOST_RECENT",
        page: Int            = 0,
        size: Int            = 20
    ): ApiResult<PaginatedData<Experiment>>

    suspend fun getExperimentById(experimentId: Long): ApiResult<ExperimentDetail>

    suspend fun createExperiment(request: ExperimentCreateRequest): ApiResult<ExperimentDetail>

    suspend fun updateExperiment(experimentId: Long, request: ExperimentUpdateRequest): ApiResult<ExperimentDetail>

    suspend fun deleteExperiment(experimentId: Long): ApiResult<Unit>

    suspend fun getUserExperiments(
        userId: Long,
        page: Int = 0,
        size: Int = 20
    ): ApiResult<PaginatedData<Experiment>>

    suspend fun getAllSubjects(): ApiResult<List<String>>
}
