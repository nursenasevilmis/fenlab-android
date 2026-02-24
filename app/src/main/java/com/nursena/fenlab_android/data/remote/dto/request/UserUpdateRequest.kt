package com.nursena.fenlab_android.data.remote.dto.request

data class UserUpdateRequest(
    val fullName: String? = null,
    val email: String? = null,
    val branch: String? = null,
    val experienceYears: Int? = null,
    val bio: String? = null,
    val profileImageUrl: String? = null
)