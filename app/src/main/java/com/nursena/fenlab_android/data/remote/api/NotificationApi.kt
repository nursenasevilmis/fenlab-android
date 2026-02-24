package com.nursena.fenlab_android.data.remote.api

import com.nursena.fenlab_android.data.remote.dto.response.NotificationResponse
import com.nursena.fenlab_android.data.remote.dto.response.PaginatedResponse
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApi {
    @GET("api/notifications")
    suspend fun getUserNotifications(@Query("page") page: Int, @Query("size") size: Int): PaginatedResponse<NotificationResponse>

    @GET("api/notifications/unread")
    suspend fun getUnreadNotifications(@Query("page") page: Int, @Query("size") size: Int): PaginatedResponse<NotificationResponse>

    @PATCH("api/notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: Long): Map<String, String>

    @PATCH("api/notifications/read-all")
    suspend fun markAllAsRead(): Map<String, String>

    @GET("api/notifications/unread/count")
    suspend fun getUnreadCount(): Map<String, Long>
}