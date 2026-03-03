package com.nursena.fenlab_android.core

import com.nursena.fenlab_android.core.Constants

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