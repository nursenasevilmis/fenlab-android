package com.nursena.fenlab_android.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class StepResponse(
    @SerializedName("id")        val id: Long,
    @SerializedName("stepOrder") val stepOrder: Int,
    @SerializedName("stepText")  val stepText: String

)