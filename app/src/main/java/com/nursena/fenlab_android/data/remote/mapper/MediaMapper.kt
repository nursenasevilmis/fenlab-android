package com.nursena.fenlab_android.data.remote.mapper

import com.nursena.fenlab_android.data.remote.dto.response.MediaResponse
import com.nursena.fenlab_android.domain.model.Media
import com.nursena.fenlab_android.domain.model.enums.MediaType

private fun String.fixMinioUrl(): String =
    this
        .replace("10.0.3.2", "10.50.232.160")
        .replace("10.0.2.2", "10.50.232.160")
        .replace("localhost", "10.50.232.160")
        .replace("127.0.0.1", "10.50.232.160")
        .replace("192.168.1.50", "10.50.232.160")
        .replace("192.168.1.108", "10.50.232.160")
        .replace("172.20.10.3", "10.50.232.160")
        .replace("192.168.1.140", "10.50.232.160")
        .replace("172.17.19.194", "10.50.232.160")

fun MediaResponse.toDomain(): Media {
    val fixed = mediaUrl.fixMinioUrl()
    android.util.Log.d("MEDIA_URL", "original=$mediaUrl  fixed=$fixed")
    return Media(
        id         = id,
        mediaType  = runCatching { MediaType.valueOf(mediaType) }.getOrDefault(MediaType.IMAGE),
        mediaUrl   = fixed,
        mediaOrder = mediaOrder
    )
}