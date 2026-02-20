package com.nursena.fenlab_android.data.remote.dto

data class ExperimentDetailResponse(
    val id: Long,
    val title: String,
    val description: String,
    val imageUrl: String,
    val authorName: String,
    val materials: List<String>,
    val steps: List<String>,
    val environment: String,
    val subject: String,
    val gradeLevel: Int,
    val difficulty: String,
    val averageRating: Double,
    val favoriteCount: Int,
    val isFavorite: Boolean,
    val createdAt: String
)