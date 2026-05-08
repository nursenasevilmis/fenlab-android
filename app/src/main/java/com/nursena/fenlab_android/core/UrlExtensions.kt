package com.nursena.fenlab_android.core

fun String?.toMinioUrl(): String? {
    if (this.isNullOrBlank()) return null
    return if (this.startsWith("http")) {
        // Zaten tam URL — olduğu gibi döndür
        this
    } else {
        // Sadece path — MinIO base URL ekle
        "${Constants.MINIO_URL}/${this.trimStart('/')}"
    }
}