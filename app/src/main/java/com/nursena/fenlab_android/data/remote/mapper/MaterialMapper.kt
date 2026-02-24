package com.nursena.fenlab_android.data.remote.mapper

import com.nursena.fenlab_android.data.remote.dto.response.MaterialResponse
import com.nursena.fenlab_android.domain.model.Material

fun MaterialResponse.toDomain(): Material = Material(
    id           = id,
    materialName = materialName,
    quantity     = quantity
)