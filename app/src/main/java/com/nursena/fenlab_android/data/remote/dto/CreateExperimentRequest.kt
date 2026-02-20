package com.nursena.fenlab_android.data.remote.dto

data class CreateExperimentRequest(
    val title: String,
    val description: String,
    val subject: String,
    val environment: String,
    val gradeLevel: Int,
    val difficulty: String,
    val materials: List<String>,
    val steps: List<String>,
    val imageUrl: String
)