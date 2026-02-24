package com.nursena.fenlab_android.data.remote.dto.request

data class RegisterRequest(
    val username: String,
    val fullName: String,
    val email: String,
    val password: String,
    val role: String,
    val branch: String? = null,
    val experienceYears: Int? = null,
    val bio: String? = null,
    val profileImageUrl: String? = null
)