package com.nursena.fenlab_android.data.remote.mapper

import com.nursena.fenlab_android.core.Constants
import com.nursena.fenlab_android.data.remote.dto.response.FileUploadResponse
import com.nursena.fenlab_android.domain.model.FileUpload

private fun String.toNormalizedUrl(): String {
    if (this.isBlank()) return this
    return if (this.startsWith("http")) this
    else "${Constants.MINIO_URL}/${this.trimStart('/')}"
}

fun FileUploadResponse.toDomain(): FileUpload = FileUpload(
    fileName    = fileName,
    fileUrl     = fileUrl.toNormalizedUrl(),
    fileSize    = fileSize,
    contentType = contentType
)