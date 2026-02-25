package com.nursena.fenlab_android.data.remote.mapper


import com.nursena.fenlab_android.data.remote.dto.response.FileUploadResponse
import com.nursena.fenlab_android.domain.model.FileUpload

fun FileUploadResponse.toDomain(): FileUpload = FileUpload(
    fileName    = fileName,
    fileUrl     = fileUrl,
    fileSize    = fileSize,
    contentType = contentType
)
