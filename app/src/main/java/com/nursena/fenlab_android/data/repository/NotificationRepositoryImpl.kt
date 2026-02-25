package com.nursena.fenlab_android.data.repository

import com.nursena.fenlab_android.core.base.BaseRepository
import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.data.remote.api.NotificationApi
import com.nursena.fenlab_android.data.remote.mapper.toDomain
import com.nursena.fenlab_android.domain.model.Notification
import com.nursena.fenlab_android.domain.model.PaginatedData
import com.nursena.fenlab_android.domain.repository.NotificationRepository
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi
) : BaseRepository(), NotificationRepository {

    override suspend fun getUserNotifications(page: Int, size: Int): ApiResult<PaginatedData<Notification>> = safeApiCall {
        notificationApi.getUserNotifications(page, size).toDomain { it.toDomain() }
    }

    override suspend fun getUnreadNotifications(page: Int, size: Int): ApiResult<PaginatedData<Notification>> = safeApiCall {
        notificationApi.getUnreadNotifications(page, size).toDomain { it.toDomain() }
    }

    override suspend fun markAsRead(notificationId: Long): ApiResult<String> = safeApiCall {
        notificationApi.markAsRead(notificationId)["message"] ?: ""
    }

    override suspend fun markAllAsRead(): ApiResult<String> = safeApiCall {
        notificationApi.markAllAsRead()["message"] ?: ""
    }

    override suspend fun getUnreadCount(): ApiResult<Long> = safeApiCall {
        notificationApi.getUnreadCount()["unreadCount"] ?: 0L
    }
}
