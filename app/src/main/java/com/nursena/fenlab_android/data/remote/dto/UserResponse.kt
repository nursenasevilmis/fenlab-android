package com.nursena.fenlab_android.data.remote.dto

data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val profileImageUrl: String?,
    val role: String,
    val createdAt: String
)