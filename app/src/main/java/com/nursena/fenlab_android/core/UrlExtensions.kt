package com.nursena.fenlab_android.core

import com.nursena.fenlab_android.core.Constants
/*
fun String?.toFullUrl(): String? {
    if (this.isNullOrBlank()) return null

    return if (this.startsWith("http")) {
        this // Zaten tam URL
    } else {
        // Başındaki /'leri temizle ve base URL ile birleştir
        val cleanPath = this.trimStart('/')
        "${Constants.BASE_URL}/$cleanPath"
    }
}
*/

fun String?.toMinioUrl(): String? {
    if (this.isNullOrBlank()) return null
    return "${Constants.MINIO_URL}/$this"
    // "fenlab-profiles/foto.jpg" → "http://192.168.1.X:9000/fenlab-profiles/foto.jpg"
}