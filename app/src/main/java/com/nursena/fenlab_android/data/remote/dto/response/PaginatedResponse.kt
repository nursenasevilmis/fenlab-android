package com.nursena.fenlab_android.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class PaginatedResponse<T>(
    @SerializedName("content")       val content: List<T>,
    @SerializedName("page")          val page: Int,
    @SerializedName("size")          val size: Int,
    @SerializedName("totalElements") val totalElements: Long,
    @SerializedName("totalPages")    val totalPages: Int,
    @SerializedName("isFirst")       val isFirst: Boolean,
    @SerializedName("isLast")        val isLast: Boolean
)