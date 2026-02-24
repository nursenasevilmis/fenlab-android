package com.nursena.fenlab_android.domain.model

data class Rating(
    val id: Long,
    val user: UserSummary,
    val rating: Int,
    val createdAt: String
)