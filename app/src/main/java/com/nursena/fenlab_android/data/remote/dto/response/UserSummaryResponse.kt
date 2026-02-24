package com.nursena.fenlab_android.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class UserSummaryResponse(
    @SerializedName("id")               val id: Long,
    @SerializedName("username")         val username: String,
    @SerializedName("fullName")         val fullName: String,       // ✅ backend'de var
    @SerializedName("role")             val role: String,           // "TEACHER" | "USER"
    @SerializedName("profileImageUrl")  val profileImageUrl: String?
)