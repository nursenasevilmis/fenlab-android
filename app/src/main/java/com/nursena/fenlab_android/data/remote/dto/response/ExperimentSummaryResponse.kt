package com.nursena.fenlab_android.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class ExperimentSummaryResponse(
    @SerializedName("id")                       val id: Long,
    @SerializedName("user")                     val user: UserSummaryResponse,
    @SerializedName("title")                    val title: String,
    @SerializedName("description")              val description: String,
    @SerializedName("gradeLevel")               val gradeLevel: Int,
    @SerializedName("subject")                  val subject: String?,
    @SerializedName("environment")              val environment: String?,
    @SerializedName("topic")                    val topic: String?,
    @SerializedName("difficulty")               val difficulty: String,
    @SerializedName("createdAt")                val createdAt: String,
    @SerializedName("thumbnailUrl")             val thumbnailUrl: String?,
    @SerializedName("videoUrl")                 val videoUrl: String?,
    @SerializedName("favoriteCount")            val favoriteCount: Long = 0,
    @SerializedName("averageRating")            val averageRating: Double?,
    @SerializedName("commentCount")             val commentCount: Long = 0,
    @SerializedName("isFavoritedByCurrentUser") val isFavoritedByCurrentUser: Boolean = false
)