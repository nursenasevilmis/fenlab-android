package com.nursena.fenlab_android.data.remote.mapper

import com.nursena.fenlab_android.data.remote.dto.response.RatingResponse
import com.nursena.fenlab_android.domain.model.Rating

fun RatingResponse.toDomain(): Rating = Rating(
    id        = id,
    user      = user.toDomain(),
    rating    = rating,
    createdAt = createdAt
)