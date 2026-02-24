package com.nursena.fenlab_android.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class RatingResponse(
    @SerializedName("id")        val id: Long,
    @SerializedName("user")      val user: UserSummaryResponse,  // ✅ EKSİKTİ
    @SerializedName("rating")    val rating: Int,                // ⚠️ score değil!
    @SerializedName("createdAt") val createdAt: String
)