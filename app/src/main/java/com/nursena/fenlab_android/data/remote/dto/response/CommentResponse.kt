package com.nursena.fenlab_android.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class CommentResponse(
    @SerializedName("id")        val id: Long,
    @SerializedName("user")      val user: UserSummaryResponse,
    @SerializedName("content")   val content: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("isOwner")   val isOwner: Boolean = false  // ✅ EKSİKTİ
)