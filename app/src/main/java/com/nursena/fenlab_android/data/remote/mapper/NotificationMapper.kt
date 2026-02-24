package com.nursena.fenlab_android.data.remote.mapper

import com.nursena.fenlab_android.data.remote.dto.response.NotificationResponse
import com.nursena.fenlab_android.domain.model.Notification
import com.nursena.fenlab_android.domain.model.enums.NotificationType

fun NotificationResponse.toDomain(): Notification = Notification(
    id           = id,
    type         = runCatching { NotificationType.valueOf(type) }.getOrDefault(NotificationType.COMMENT),
    message      = message,
    isRead       = isRead,
    experimentId = experimentId,
    relatedUser  = relatedUser?.toDomain(),
    createdAt    = createdAt
)