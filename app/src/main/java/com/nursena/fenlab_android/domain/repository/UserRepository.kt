package com.nursena.fenlab_android.domain.repository

import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.data.remote.dto.request.UserUpdateRequest
import com.nursena.fenlab_android.domain.model.User

interface UserRepository {

    suspend fun getCurrentUser(): ApiResult<User>

    suspend fun getUserById(userId: Long): ApiResult<User>

    suspend fun updateUser(userId: Long, request: UserUpdateRequest): ApiResult<User>

    suspend fun deleteUser(userId: Long): ApiResult<Unit>
}
