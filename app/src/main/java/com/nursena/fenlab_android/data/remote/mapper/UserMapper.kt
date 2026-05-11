package com.nursena.fenlab_android.data.remote.mapper

import com.nursena.fenlab_android.core.Constants
import com.nursena.fenlab_android.data.remote.dto.response.UserResponse
import com.nursena.fenlab_android.data.remote.dto.response.UserSummaryResponse
import com.nursena.fenlab_android.domain.model.User
import com.nursena.fenlab_android.domain.model.UserSummary
import com.nursena.fenlab_android.domain.model.enums.UserRole

private fun String?.toProfileImageUrl(): String? {
    if (this.isNullOrBlank()) return null

    val value = this.trim()

    val buckets = listOf(
        "fenlab-images/",
        "fenlab-videos/",
        "fenlab-profiles/",
        "fenlab-pdfs/"
    )

    val path = buckets.firstNotNullOfOrNull { bucket ->
        val index = value.indexOf(bucket)
        if (index != -1) value.substring(index) else null
    }

    return if (path != null) {
        "${Constants.MEDIA_BASE_URL}/${path.trimStart('/')}"
    } else if (value.startsWith("http")) {
        value
    } else {
        "${Constants.MEDIA_BASE_URL}/${value.trimStart('/')}"
    }
}

fun UserResponse.toDomain(): User = User(
    id               = id,
    username         = username,
    fullName         = fullName,
    email            = email,
    role             = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.USER),
    branch           = branch,
    experienceYears  = experienceYears,
    bio              = bio,
    profileImageUrl  = profileImageUrl.toProfileImageUrl(),
    createdAt        = createdAt,
    lastLogin        = lastLogin,
    experimentCount  = experimentCount
)

fun UserSummaryResponse.toDomain(): UserSummary = UserSummary(
    id              = id,
    username        = username,
    fullName        = fullName,
    role            = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.USER),
    profileImageUrl = profileImageUrl.toProfileImageUrl()
)