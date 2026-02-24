package com.nursena.fenlab_android.data.remote.dto.request

data class ExperimentUpdateRequest(
    val title: String? = null,
    val description: String? = null,
    val gradeLevel: Int? = null,
    val subject: String? = null,
    val environment: String? = null,
    val topic: String? = null,
    val difficulty: String? = null,
    val expectedResult: String? = null,
    val safetyNotes: String? = null,
    val isPublished: Boolean? = null,
    val materials: List<MaterialRequest>? = null,
    val steps: List<StepRequest>? = null,
    val media: List<MediaRequest>? = null
)