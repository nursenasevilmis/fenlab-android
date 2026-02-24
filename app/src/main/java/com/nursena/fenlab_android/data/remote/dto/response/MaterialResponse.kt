package com.nursena.fenlab_android.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class MaterialResponse(
    @SerializedName("id")           val id: Long,
    @SerializedName("materialName") val materialName: String,   // ⚠️ name değil!
    @SerializedName("quantity")     val quantity: String        // String, nullable değil
)