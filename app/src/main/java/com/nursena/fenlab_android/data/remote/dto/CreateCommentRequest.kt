package com.nursena.fenlab_android.data.remote.dto

data class CreateCommentRequest(
    val experimentId: Long,
    val content: String
)