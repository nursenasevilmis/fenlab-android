package com.nursena.fenlab_android.data.remote.api

import com.nursena.fenlab_android.data.remote.dto.response.FileUploadResponse
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface FileUploadApi {
    @Multipart
    @POST("api/files/upload/image")
    suspend fun uploadImage(@Part("file") file: MultipartBody.Part): FileUploadResponse

    @Multipart
    @POST("api/files/upload/video")
    suspend fun uploadVideo(@Part("file") file: MultipartBody.Part): FileUploadResponse

    @Multipart
    @POST("api/files/upload/profile")
    suspend fun uploadProfileImage(@Part("file") file: MultipartBody.Part): FileUploadResponse

    @DELETE("api/files/delete/image")
    suspend fun deleteImage(@Query("url") imageUrl: String)

    @DELETE("api/files/delete/video")
    suspend fun deleteVideo(@Query("url") videoUrl: String)
}