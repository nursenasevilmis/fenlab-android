package com.nursena.fenlab_android.domain.model

data class Comment(
    val id: Long,
    val author: UserSummary,
    val content: String,
    val createdAt: String,
    val updatedAt: String?,
    val isOwner: Boolean
)