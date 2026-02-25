package com.nursena.fenlab_android.data.repository

import com.nursena.fenlab_android.core.base.BaseRepository
import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.data.remote.api.*
import com.nursena.fenlab_android.data.remote.dto.request.*
import com.nursena.fenlab_android.data.remote.mapper.toDomain
import com.nursena.fenlab_android.domain.model.*
import com.nursena.fenlab_android.domain.repository.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi
) : BaseRepository(), AuthRepository {

    override suspend fun login(request: LoginRequest): ApiResult<Pair<String, User>> =
        safeApiCall {
            val response = authApi.login(request)
            Pair(response.token, response.user.toDomain())
        }

    override suspend fun register(request: RegisterRequest): ApiResult<Pair<String, User>> =
        safeApiCall {
            val response = authApi.register(request)
            Pair(response.token, response.user.toDomain())
        }
}