package com.nursena.fenlab_android.data.repository

import com.nursena.fenlab_android.core.base.BaseRepository
import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.data.remote.api.FileUploadApi
import com.nursena.fenlab_android.data.remote.mapper.toDomain
import com.nursena.fenlab_android.domain.model.FileUpload
import com.nursena.fenlab_android.domain.repository.FileUploadRepository
import okhttp3.MultipartBody
import javax.inject.Inject

class FileUploadRepositoryImpl @Inject constructor(
    private val fileUploadApi: FileUploadApi
) : BaseRepository(), FileUploadRepository {

    override suspend fun uploadImage(file: MultipartBody.Part): ApiResult<FileUpload> = safeApiCall {
        fileUploadApi.uploadImage(file).toDomain()
    }

    override suspend fun uploadVideo(file: MultipartBody.Part): ApiResult<FileUpload> = safeApiCall {
        fileUploadApi.uploadVideo(file).toDomain()
    }

    override suspend fun uploadProfileImage(file: MultipartBody.Part): ApiResult<FileUpload> = safeApiCall {
        fileUploadApi.uploadProfileImage(file).toDomain()
    }

    override suspend fun deleteImage(imageUrl: String): ApiResult<Unit> = safeApiCall {
        fileUploadApi.deleteImage(imageUrl)
    }

    override suspend fun deleteVideo(videoUrl: String): ApiResult<Unit> = safeApiCall {
        fileUploadApi.deleteVideo(videoUrl)
    }
}
