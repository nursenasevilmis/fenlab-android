package com.nursena.fenlab_android.domain.model

import com.nursena.fenlab_android.domain.model.enums.DifficultyLevel
import com.nursena.fenlab_android.domain.model.enums.EnvironmentType
import com.nursena.fenlab_android.domain.model.enums.SubjectType

data class Experiment(
    val id: Long,
    val author: UserSummary,
    val title: String,
    val description: String,
    val gradeLevel: Int,
    val subject: SubjectType?,
    val environment: EnvironmentType?,
    val topic: String?,
    val difficulty: DifficultyLevel,
    val createdAt: String,
    val thumbnailUrl: String?,
    val videoUrl: String?,
    val favoriteCount: Long,
    val averageRating: Double?,
    val commentCount: Long,
    val isFavoritedByCurrentUser: Boolean
) {
    val displaySubject: String     get() = subject?.toDisplayString() ?: "Diğer"
    val displayDifficulty: String  get() = difficulty.toDisplayString()
    val displayEnvironment: String get() = environment?.toDisplayString() ?: ""
    val displayRating: String      get() = averageRating?.let { "%.1f".format(it) } ?: "-"
}