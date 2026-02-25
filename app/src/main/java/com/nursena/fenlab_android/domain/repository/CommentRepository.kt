package com.nursena.fenlab_android.domain.repository

import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.data.remote.dto.request.CommentCreateRequest
import com.nursena.fenlab_android.data.remote.dto.request.CommentUpdateRequest
import com.nursena.fenlab_android.domain.model.Comment
import com.nursena.fenlab_android.domain.model.PaginatedData

interface CommentRepository {

    suspend fun getExperimentComments(
        experimentId: Long,
        page: Int = 0,
        size: Int = 10
    ): ApiResult<PaginatedData<Comment>>

    suspend fun addComment(experimentId: Long, request: CommentCreateRequest): ApiResult<Comment>

    suspend fun updateComment(commentId: Long, request: CommentUpdateRequest): ApiResult<Comment>

    suspend fun deleteComment(commentId: Long): ApiResult<Unit>
}
