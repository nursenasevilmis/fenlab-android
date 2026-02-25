package com.nursena.fenlab_android.domain.model


data class FileUpload(
    val fileName: String,
    val fileUrl: String,
    val fileSize: Long,
    val contentType: String
)
