package com.nursena.fenlab_android.data.remote.mapper

import com.nursena.fenlab_android.data.remote.dto.response.UserResponse
import com.nursena.fenlab_android.data.remote.dto.response.UserSummaryResponse
import com.nursena.fenlab_android.domain.model.User
import com.nursena.fenlab_android.domain.model.UserSummary
import com.nursena.fenlab_android.domain.model.enums.UserRole

fun UserResponse.toDomain(): User = User(
    id               = id,
    username         = username,
    fullName         = fullName,
    email            = email,
    role             = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.USER),
    branch           = branch,
    experienceYears  = experienceYears,
    bio              = bio,
    profileImageUrl  = profileImageUrl,
    createdAt        = createdAt,
    lastLogin        = lastLogin,
    experimentCount  = experimentCount
)

fun UserSummaryResponse.toDomain(): UserSummary = UserSummary(
    id              = id,
    username        = username,
    fullName        = fullName,
    role            = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.USER),
    profileImageUrl = profileImageUrl
)