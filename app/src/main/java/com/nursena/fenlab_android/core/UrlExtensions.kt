package com.nursena.fenlab_android.core

import com.nursena.fenlab_android.core.Constants

private val ipPattern = Regex(
    """(10\.0\.[23]\.2|localhost|127\.0\.0\.1|192\.168\.\d+\.\d+|172\.\d+\.\d+\.\d+)"""
)

/**
 * MinIO path veya tam URL → aktif IP ile tam URL
 * Eğer zaten http:// ile başlıyorsa sadece IP'yi düzeltir.
 * Eğer sadece path ise MinIO base URL ekler.
 */
fun String?.toMinioUrl(): String? {
    if (this.isNullOrBlank()) return null
    return if (this.startsWith("http")) {
        // Zaten tam URL — sadece IP'yi aktif sunucu IP'siyle değiştir
        ipPattern.replace(this, Constants.SERVER_IP)
    } else {
        // Sadece path — base URL ekle
        "${Constants.MINIO_URL}/${this.trimStart('/')}"
    }
}
