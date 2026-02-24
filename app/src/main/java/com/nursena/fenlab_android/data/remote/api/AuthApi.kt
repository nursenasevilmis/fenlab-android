package com.nursena.fenlab_android.data.remote.api

import com.nursena.fenlab_android.data.remote.dto.*
import com.nursena.fenlab_android.data.remote.dto.request.LoginRequest
import com.nursena.fenlab_android.data.remote.dto.request.RegisterRequest
import com.nursena.fenlab_android.data.remote.dto.response.AuthResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): AuthResponse

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): AuthResponse
}