package com.nursena.fenlab_android.data.remote.mapper

import com.nursena.fenlab_android.data.remote.dto.response.CommentResponse
import com.nursena.fenlab_android.domain.model.Comment

fun CommentResponse.toDomain(): Comment = Comment(
    id        = id,
    author    = user.toDomain(),
    content   = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isOwner   = isOwner
)