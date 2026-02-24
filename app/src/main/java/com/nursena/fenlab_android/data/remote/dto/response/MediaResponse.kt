package com.nursena.fenlab_android.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class MediaResponse(
    @SerializedName("id")         val id: Long,
    @SerializedName("mediaType")  val mediaType: String,
    @SerializedName("mediaUrl")   val mediaUrl: String,
    @SerializedName("mediaOrder") val mediaOrder: Int
)