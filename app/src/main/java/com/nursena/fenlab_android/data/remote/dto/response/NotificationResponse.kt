package com.nursena.fenlab_android.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    @SerializedName("id")           val id: Long,
    @SerializedName("type")         val type: String,
    @SerializedName("message")      val message: String,
    @SerializedName("isRead")       val isRead: Boolean,
    @SerializedName("experimentId") val experimentId: Long?,
    @SerializedName("relatedUser")  val relatedUser: UserSummaryResponse?,
    @SerializedName("createdAt")    val createdAt: String
)