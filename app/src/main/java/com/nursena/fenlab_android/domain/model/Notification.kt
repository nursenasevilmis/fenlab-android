package com.nursena.fenlab_android.domain.model

import com.nursena.fenlab_android.domain.model.enums.NotificationType

data class Notification(
    val id: Long,
    val type: NotificationType,
    val message: String,
    val isRead: Boolean,
    val experimentId: Long?,
    val relatedUser: UserSummary?,
    val createdAt: String
) {
    val icon: String get() = when (type) {
        NotificationType.COMMENT  -> "💬"
        NotificationType.QUESTION -> "❓"
        NotificationType.ANSWER   -> "✅"
        NotificationType.FAVORITE -> "❤️"
    }
}