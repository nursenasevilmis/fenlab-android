package com.nursena.fenlab_android.domain.model

import com.nursena.fenlab_android.domain.model.enums.MediaType

data class Media(
    val id: Long,
    val mediaType: MediaType,
    val mediaUrl: String,
    val mediaOrder: Int
) {
    val isVideo: Boolean get() = mediaType == MediaType.VIDEO
    val isImage: Boolean get() = mediaType == MediaType.IMAGE
}