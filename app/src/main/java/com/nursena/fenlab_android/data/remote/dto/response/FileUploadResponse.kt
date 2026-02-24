package com.nursena.fenlab_android.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class FileUploadResponse(
    @SerializedName("fileName")    val fileName: String,
    @SerializedName("fileUrl")     val fileUrl: String,
    @SerializedName("fileSize")    val fileSize: Long,
    @SerializedName("contentType") val contentType: String
)