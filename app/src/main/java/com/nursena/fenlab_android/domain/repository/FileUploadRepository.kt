package com.nursena.fenlab_android.domain.repository


import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.domain.model.FileUpload
import okhttp3.MultipartBody

interface FileUploadRepository {

    suspend fun uploadImage(file: MultipartBody.Part): ApiResult<FileUpload>

    suspend fun uploadVideo(file: MultipartBody.Part): ApiResult<FileUpload>

    suspend fun uploadProfileImage(file: MultipartBody.Part): ApiResult<FileUpload>

    suspend fun deleteImage(imageUrl: String): ApiResult<Unit>

    suspend fun deleteVideo(videoUrl: String): ApiResult<Unit>
}
