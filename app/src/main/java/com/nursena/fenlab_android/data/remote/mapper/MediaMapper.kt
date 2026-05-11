package com.nursena.fenlab_android.data.remote.mapper

import com.nursena.fenlab_android.core.Constants
import com.nursena.fenlab_android.data.remote.dto.response.MediaResponse
import com.nursena.fenlab_android.domain.model.Media
import com.nursena.fenlab_android.domain.model.enums.MediaType

private fun String.toMediaUrl(): String {
    if (this.isBlank()) return this
    return if (this.startsWith("http")) this
    else "${Constants.MINIO_URL}/${this.trimStart('/')}"
}

fun MediaResponse.toDomain(): Media = Media(
    id         = id,
    mediaType  = runCatching { MediaType.valueOf(mediaType) }.getOrDefault(MediaType.IMAGE),
    mediaUrl   = mediaUrl.toMediaUrl(),
    mediaOrder = mediaOrder
)