package com.nursena.fenlab_android.data.remote.mapper

import com.nursena.fenlab_android.core.Constants
import com.nursena.fenlab_android.data.remote.dto.response.FileUploadResponse
import com.nursena.fenlab_android.domain.model.FileUpload

private val ipPattern = Regex(
    """(10\.0\.[23]\.2|localhost|127\.0\.0\.1|192\.168\.\d+\.\d+|172\.\d+\.\d+\.\d+)"""
)

/**
 * Backend'den gelen fileUrl ham path ("fenlab-images/xxx.jpg") veya tam URL olabilir.
 * Her iki durumu da normalize ediyoruz, böylece AsyncImage doğru URL'yi kullanır.
 */
private fun String.toNormalizedUrl(): String {
    if (this.isBlank()) return this
    return if (this.startsWith("http")) {
        // Tam URL — sadece IP'yi güncelle
        ipPattern.replace(this, Constants.SERVER_IP)
    } else {
        // Sadece path — Minio base ekle
        "${Constants.MINIO_URL}/${this.trimStart('/')}"
    }
}

fun FileUploadResponse.toDomain(): FileUpload = FileUpload(
    fileName    = fileName,
    fileUrl     = fileUrl.toNormalizedUrl(),
    fileSize    = fileSize,
    contentType = contentType
)