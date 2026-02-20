package com.nursena.fenlab_android.data.remote.dto

data class RatingRequest(
    val experimentId: Long,
    val score: Int
)