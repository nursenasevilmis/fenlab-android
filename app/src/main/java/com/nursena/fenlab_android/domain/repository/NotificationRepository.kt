package com.nursena.fenlab_android.domain.repository

import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.domain.model.Notification
import com.nursena.fenlab_android.domain.model.PaginatedData

interface NotificationRepository {

    suspend fun getUserNotifications(page: Int = 0, size: Int = 10): ApiResult<PaginatedData<Notification>>

    suspend fun getUnreadNotifications(page: Int = 0, size: Int = 10): ApiResult<PaginatedData<Notification>>

    suspend fun markAsRead(notificationId: Long): ApiResult<String>

    suspend fun markAllAsRead(): ApiResult<String>

    suspend fun getUnreadCount(): ApiResult<Long>
}
