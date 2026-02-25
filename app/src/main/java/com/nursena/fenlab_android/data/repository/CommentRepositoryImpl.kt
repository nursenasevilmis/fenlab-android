package com.nursena.fenlab_android.data.repository

import com.nursena.fenlab_android.core.base.BaseRepository
import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.data.remote.api.CommentApi
import com.nursena.fenlab_android.data.remote.dto.request.CommentCreateRequest
import com.nursena.fenlab_android.data.remote.dto.request.CommentUpdateRequest
import com.nursena.fenlab_android.data.remote.mapper.toDomain
import com.nursena.fenlab_android.domain.model.Comment
import com.nursena.fenlab_android.domain.model.PaginatedData
import com.nursena.fenlab_android.domain.repository.CommentRepository
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val commentApi: CommentApi
) : BaseRepository(), CommentRepository {

    override suspend fun getExperimentComments(
        experimentId: Long,
        page: Int,
        size: Int
    ): ApiResult<PaginatedData<Comment>> = safeApiCall {
        commentApi.getExperimentComments(experimentId, page, size).toDomain { it.toDomain() }
    }

    override suspend fun addComment(experimentId: Long, request: CommentCreateRequest): ApiResult<Comment> = safeApiCall {
        commentApi.addComment(experimentId, request).toDomain()
    }

    override suspend fun updateComment(commentId: Long, request: CommentUpdateRequest): ApiResult<Comment> = safeApiCall {
        commentApi.updateComment(commentId, request).toDomain()
    }

    override suspend fun deleteComment(commentId: Long): ApiResult<Unit> = safeApiCall {
        commentApi.deleteComment(commentId)
    }
}
