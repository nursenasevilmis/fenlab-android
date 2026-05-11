package com.nursena.fenlab_android.data.remote.mapper

import com.nursena.fenlab_android.core.Constants
import com.nursena.fenlab_android.data.remote.dto.response.MediaResponse
import com.nursena.fenlab_android.domain.model.Media
import com.nursena.fenlab_android.domain.model.enums.MediaType

private fun String.toMediaUrl(): String {
    val value = this.trim()
    if (value.isBlank()) return value

    val buckets = listOf(
        "fenlab-images/",
        "fenlab-videos/",
        "fenlab-profiles/",
        "fenlab-pdfs/"
    )

    val path = buckets.firstNotNullOfOrNull { bucket ->
        val index = value.indexOf(bucket)
        if (index != -1) value.substring(index) else null
    }

    return if (path != null) {
        "${Constants.MEDIA_BASE_URL}/${path.trimStart('/')}"
    } else if (value.startsWith("http")) {
        value
    } else {
        "${Constants.MEDIA_BASE_URL}/${value.trimStart('/')}"
    }
}

fun MediaResponse.toDomain(): Media = Media(
    id         = id,
    mediaType  = runCatching { MediaType.valueOf(mediaType) }.getOrDefault(MediaType.IMAGE),
    mediaUrl   = mediaUrl.toMediaUrl(),
    mediaOrder = mediaOrder
)