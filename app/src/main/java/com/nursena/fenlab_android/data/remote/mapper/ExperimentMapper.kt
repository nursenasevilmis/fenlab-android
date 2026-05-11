package com.nursena.fenlab_android.data.remote.mapper

import com.nursena.fenlab_android.core.Constants
import com.nursena.fenlab_android.data.remote.dto.response.ExperimentDetailResponse
import com.nursena.fenlab_android.data.remote.dto.response.ExperimentSummaryResponse
import com.nursena.fenlab_android.domain.model.Experiment
import com.nursena.fenlab_android.domain.model.ExperimentDetail
import com.nursena.fenlab_android.domain.model.enums.DifficultyLevel
import com.nursena.fenlab_android.domain.model.enums.EnvironmentType
import com.nursena.fenlab_android.domain.model.enums.SubjectType

private fun parseSubject(raw: String?): SubjectType? =
    raw?.let { runCatching { SubjectType.valueOf(it.uppercase()) }.getOrNull() }

private fun customSubjectOf(raw: String?, parsed: SubjectType?): String? =
    if (raw != null && parsed == null) raw else null

private fun String?.toMediaUrl(): String? {
    if (this.isNullOrBlank()) return null
    return if (this.startsWith("http")) this
    else "${Constants.MINIO_URL}/${this.trimStart('/')}"
}

fun ExperimentSummaryResponse.toDomain(): Experiment {
    val parsedSubject = parseSubject(subject)
    return Experiment(
        id                       = id,
        author                   = user.toDomain(),
        title                    = title,
        description              = description,
        gradeLevel               = gradeLevel,
        subject                  = parsedSubject,
        customSubject            = customSubjectOf(subject, parsedSubject),
        environment              = environment?.let { runCatching { EnvironmentType.valueOf(it) }.getOrNull() },
        topic                    = topic,
        difficulty               = runCatching { DifficultyLevel.valueOf(difficulty) }.getOrDefault(DifficultyLevel.MEDIUM),
        createdAt                = createdAt,
        thumbnailUrl             = thumbnailUrl.toMediaUrl(),
        videoUrl                 = videoUrl.toMediaUrl(),
        favoriteCount            = favoriteCount,
        averageRating            = averageRating,
        commentCount             = commentCount,
        isFavoritedByCurrentUser = isFavoritedByCurrentUser
    )
}

fun ExperimentDetailResponse.toDomain(): ExperimentDetail {
    val parsedSubject = parseSubject(subject)
    return ExperimentDetail(
        id                       = id,
        author                   = user.toDomain(),
        title                    = title,
        description              = description,
        gradeLevel               = gradeLevel,
        subject                  = parsedSubject,
        customSubject            = customSubjectOf(subject, parsedSubject),
        environment              = environment?.let { runCatching { EnvironmentType.valueOf(it) }.getOrNull() },
        topic                    = topic,
        difficulty               = runCatching { DifficultyLevel.valueOf(difficulty) }.getOrDefault(DifficultyLevel.MEDIUM),
        expectedResult           = expectedResult,
        safetyNotes              = safetyNotes,
        isPublished              = isPublished,
        createdAt                = createdAt,
        updatedAt                = updatedAt,
        materials                = materials.map { it.toDomain() },
        steps                    = steps.map { it.toDomain() },
        media                    = media.map { it.toDomain() },
        favoriteCount            = favoriteCount,
        averageRating            = averageRating,
        commentCount             = commentCount,
        questionCount            = questionCount,
        isFavoritedByCurrentUser = isFavoritedByCurrentUser,
        currentUserRating        = currentUserRating
    )
}