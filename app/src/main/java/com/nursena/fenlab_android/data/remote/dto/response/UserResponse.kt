package com.nursena.fenlab_android.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("id")               val id: Long,
    @SerializedName("username")         val username: String,
    @SerializedName("fullName")         val fullName: String,
    @SerializedName("email")            val email: String,
    @SerializedName("role")             val role: String,           // "TEACHER" | "USER"
    @SerializedName("branch")           val branch: String?,
    @SerializedName("experienceYears")  val experienceYears: Int?,
    @SerializedName("bio")              val bio: String?,
    @SerializedName("profileImageUrl")  val profileImageUrl: String?,
    @SerializedName("createdAt")        val createdAt: String,
    @SerializedName("lastLogin")        val lastLogin: String?,
    @SerializedName("experimentCount")  val experimentCount: Long = 0
)