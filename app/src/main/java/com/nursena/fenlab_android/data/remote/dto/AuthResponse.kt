package com.nursena.fenlab_android.data.remote.dto

data class AuthResponse(
    val token: String,
    val userId: Long,
    val name: String,
    val role: String
)