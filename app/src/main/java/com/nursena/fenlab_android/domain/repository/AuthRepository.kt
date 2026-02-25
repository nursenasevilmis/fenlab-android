package com.nursena.fenlab_android.domain.repository

import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.data.remote.dto.request.LoginRequest
import com.nursena.fenlab_android.data.remote.dto.request.RegisterRequest
import com.nursena.fenlab_android.domain.model.User

interface AuthRepository {
    suspend fun login(request: LoginRequest): ApiResult<Pair<String, User>>
    suspend fun register(request: RegisterRequest): ApiResult<Pair<String, User>>
}
