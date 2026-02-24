package com.nursena.fenlab_android.data.remote.mapper

import com.nursena.fenlab_android.data.remote.dto.response.ExperimentDetailResponse
import com.nursena.fenlab_android.data.remote.dto.response.ExperimentSummaryResponse
import com.nursena.fenlab_android.domain.model.Experiment
import com.nursena.fenlab_android.domain.model.ExperimentDetail
import com.nursena.fenlab_android.domain.model.enums.DifficultyLevel
import com.nursena.fenlab_android.domain.model.enums.EnvironmentType
import com.nursena.fenlab_android.domain.model.enums.SubjectType

fun ExperimentSummaryResponse.toDomain(): Experiment = Experiment(
    id                       = id,
    author                   = user.toDomain(),
    title                    = title,
    description              = description,
    gradeLevel               = gradeLevel,
    subject                  = subject?.let { runCatching { SubjectType.valueOf(it) }.getOrNull() },
    environment              = environment?.let { runCatching { EnvironmentType.valueOf(it) }.getOrNull() },
    topic                    = topic,
    difficulty               = runCatching { DifficultyLevel.valueOf(difficulty) }.getOrDefault(DifficultyLevel.MEDIUM),
    createdAt                = createdAt,
    thumbnailUrl             = thumbnailUrl,
    videoUrl                 = videoUrl,
    favoriteCount            = favoriteCount,
    averageRating            = averageRating,
    commentCount             = commentCount,
    isFavoritedByCurrentUser = isFavoritedByCurrentUser
)

fun ExperimentDetailResponse.toDomain(): ExperimentDetail = ExperimentDetail(
    id                       = id,
    author                   = user.toDomain(),
    title                    = title,
    description              = description,
    gradeLevel               = gradeLevel,
    subject                  = subject?.let { runCatching { SubjectType.valueOf(it) }.getOrNull() },
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