package com.nursena.fenlab_android.data.remote.dto.request

data class MediaRequest(
    val mediaType: String,
    val mediaUrl: String,
    val mediaOrder: Int = 0
)