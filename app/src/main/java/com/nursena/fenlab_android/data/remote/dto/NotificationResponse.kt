package com.nursena.fenlab_android.data.remote.dto

data class NotificationResponse(
    val id: Long,
    val message: String,
    val isRead: Boolean,
    val createdAt: String
)