package com.nursena.fenlab_android.data.remote.mapper

import com.nursena.fenlab_android.data.remote.dto.response.MediaResponse
import com.nursena.fenlab_android.domain.model.Media
import com.nursena.fenlab_android.domain.model.enums.MediaType

private fun String.fixMinioUrl(): String =
    this
        .replace("10.0.3.2", "192.168.1.108")
        .replace("10.0.2.2", "192.168.1.108")
        .replace("localhost", "192.168.1.108")
        .replace("127.0.0.1", "192.168.1.108")
        .replace("192.168.1.50", "192.168.1.108")

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