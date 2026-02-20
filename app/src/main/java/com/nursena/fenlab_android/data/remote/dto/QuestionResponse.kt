package com.nursena.fenlab_android.data.remote.dto

data class QuestionResponse(
    val id: Long,
    val userName: String,
    val question: String,
    val answer: String?,
    val createdAt: String
)
