package com.nursena.fenlab_android.data.remote.api

import com.nursena.fenlab_android.data.remote.dto.request.UserUpdateRequest
import com.nursena.fenlab_android.data.remote.dto.response.UserResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserApi {

    @GET("api/users/me")
    suspend fun getCurrentUser(): UserResponse

    @GET("api/users/{userId}")
    suspend fun getUserById(
        @Path("userId") userId: Long
    ): UserResponse

    @PUT("api/users/{userId}")
    suspend fun updateUser(
        @Path("userId") userId: Long,
        @Body request: UserUpdateRequest
    ): UserResponse

    @DELETE("api/users/{userId}")
    suspend fun deleteUser(
        @Path("userId") userId: Long
    )
}