package com.nursena.fenlab_android.data.remote.dto

data class CommentResponse(
    val id: Long,
    val userName: String,
    val content: String,
    val createdAt: String
)