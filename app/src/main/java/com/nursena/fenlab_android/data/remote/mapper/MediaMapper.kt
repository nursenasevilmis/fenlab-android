package com.nursena.fenlab_android.data.remote.mapper

import com.nursena.fenlab_android.core.Constants
import com.nursena.fenlab_android.data.remote.dto.response.MediaResponse
import com.nursena.fenlab_android.domain.model.Media
import com.nursena.fenlab_android.domain.model.enums.MediaType

private val ipPattern = Regex(
    """(10\.0\.[23]\.2|localhost|127\.0\.0\.1|192\.168\.\d+\.\d+|172\.\d+\.\d+\.\d+)"""
)

private fun String.fixMinioUrl(): String =
    ipPattern.replace(this, Constants.SERVER_IP)

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