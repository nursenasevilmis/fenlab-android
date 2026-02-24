package com.nursena.fenlab_android.domain.model

import com.nursena.fenlab_android.domain.model.enums.DifficultyLevel
import com.nursena.fenlab_android.domain.model.enums.EnvironmentType
import com.nursena.fenlab_android.domain.model.enums.SubjectType

data class ExperimentDetail(
    val id: Long,
    val author: UserSummary,
    val title: String,
    val description: String,
    val gradeLevel: Int,
    val subject: SubjectType?,
    val environment: EnvironmentType?,
    val topic: String?,
    val difficulty: DifficultyLevel,
    val expectedResult: String?,
    val safetyNotes: String?,
    val isPublished: Boolean,
    val createdAt: String,
    val updatedAt: String?,
    val materials: List<Material>,
    val steps: List<Step>,
    val media: List<Media>,
    val favoriteCount: Long,
    val averageRating: Double?,
    val commentCount: Long,
    val questionCount: Long,
    val isFavoritedByCurrentUser: Boolean,
    val currentUserRating: Int?
) {
    val sortedSteps: List<Step>      get() = steps.sortedBy { it.stepOrder }
    val videoMedia: Media?           get() = media.filter { it.isVideo }.minByOrNull { it.mediaOrder }
    val imageMediaList: List<Media>  get() = media.filter { it.isImage }.sortedBy { it.mediaOrder }
    val displaySubject: String       get() = subject?.toDisplayString() ?: "Diğer"
    val displayDifficulty: String    get() = difficulty.toDisplayString()
    val displayEnvironment: String   get() = environment?.toDisplayString() ?: ""
    val displayRating: String        get() = averageRating?.let { "%.1f".format(it) } ?: "-"
}