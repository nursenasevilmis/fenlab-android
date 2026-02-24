package com.nursena.fenlab_android.data.remote.dto.request

data class ExperimentCreateRequest(
    val title: String,
    val description: String,
    val gradeLevel: Int,
    val subject: String? = null,
    val environment: String? = null,
    val topic: String? = null,
    val difficulty: String,
    val expectedResult: String? = null,
    val safetyNotes: String? = null,
    val isPublished: Boolean = true,
    val materials: List<MaterialRequest> = emptyList(),
    val steps: List<StepRequest> = emptyList(),
    val media: List<MediaRequest> = emptyList()
)