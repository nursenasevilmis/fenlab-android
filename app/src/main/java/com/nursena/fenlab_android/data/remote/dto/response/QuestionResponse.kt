package com.nursena.fenlab_android.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class QuestionResponse(
    @SerializedName("id")           val id: Long,
    @SerializedName("asker")        val asker: UserSummaryResponse,     // ⚠️ askedBy değil!
    @SerializedName("questionText") val questionText: String,           // ⚠️ content değil!
    @SerializedName("answerText")   val answerText: String?,            // ⚠️ answer değil!
    @SerializedName("answerer")     val answerer: UserSummaryResponse?, // ✅ EKSİKTİ
    @SerializedName("createdAt")    val createdAt: String,
    @SerializedName("answeredAt")   val answeredAt: String?,            // ✅ EKSİKTİ
    @SerializedName("isAnswered")   val isAnswered: Boolean = false,
    @SerializedName("canAnswer")    val canAnswer: Boolean = false      // ✅ EKSİKTİ
)