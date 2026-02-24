package com.nursena.fenlab_android.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class ExperimentDetailResponse(
    @SerializedName("id")                       val id: Long,
    @SerializedName("user")                     val user: UserSummaryResponse,
    @SerializedName("title")                    val title: String,
    @SerializedName("description")              val description: String,
    @SerializedName("gradeLevel")               val gradeLevel: Int,
    @SerializedName("subject")                  val subject: String?,
    @SerializedName("environment")              val environment: String?,
    @SerializedName("topic")                    val topic: String?,
    @SerializedName("difficulty")               val difficulty: String,
    @SerializedName("expectedResult")           val expectedResult: String?,
    @SerializedName("safetyNotes")              val safetyNotes: String?,
    @SerializedName("isPublished")              val isPublished: Boolean,
    @SerializedName("createdAt")                val createdAt: String,
    @SerializedName("updatedAt")                val updatedAt: String?,
    @SerializedName("materials")                val materials: List<MaterialResponse>,
    @SerializedName("steps")                    val steps: List<StepResponse>,
    @SerializedName("media")                    val media: List<MediaResponse>,
    @SerializedName("favoriteCount")            val favoriteCount: Long = 0,
    @SerializedName("averageRating")            val averageRating: Double?,
    @SerializedName("commentCount")             val commentCount: Long = 0,
    @SerializedName("questionCount")            val questionCount: Long = 0,
    @SerializedName("isFavoritedByCurrentUser") val isFavoritedByCurrentUser: Boolean = false,
    @SerializedName("currentUserRating")        val currentUserRating: Int?
)
