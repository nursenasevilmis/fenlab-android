package com.nursena.fenlab_android.data.repository

import com.nursena.fenlab_android.core.base.BaseRepository
import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.data.remote.api.UserApi
import com.nursena.fenlab_android.data.remote.dto.request.UserUpdateRequest
import com.nursena.fenlab_android.data.remote.mapper.toDomain
import com.nursena.fenlab_android.domain.model.User
import com.nursena.fenlab_android.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi
) : BaseRepository(), UserRepository {

    override suspend fun getCurrentUser(): ApiResult<User> = safeApiCall {
        userApi.getCurrentUser().toDomain()
    }

    override suspend fun getUserById(userId: Long): ApiResult<User> = safeApiCall {
        userApi.getUserById(userId).toDomain()
    }

    override suspend fun updateUser(userId: Long, request: UserUpdateRequest): ApiResult<User> = safeApiCall {
        userApi.updateUser(userId, request).toDomain()
    }

    override suspend fun deleteUser(userId: Long): ApiResult<Unit> = safeApiCall {
        userApi.deleteUser(userId)
    }
}
