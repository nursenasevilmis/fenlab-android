package com.nursena.fenlab_android.data.remote.dto

data class CreateQuestionRequest(
    val experimentId: Long,
    val question: String
)