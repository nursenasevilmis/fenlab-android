package com.nursena.fenlab_android.data.remote.dto

data class ExperimentSummaryResponse(
    val id: Long,
    val title: String,
    val description: String,
    val imageUrl: String,
    val authorName: String,
    val averageRating: Double,
    val favoriteCount: Int,
    val isFavorite: Boolean
)
