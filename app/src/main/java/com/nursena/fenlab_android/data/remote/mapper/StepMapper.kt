package com.nursena.fenlab_android.data.remote.mapper

import com.nursena.fenlab_android.data.remote.dto.response.StepResponse
import com.nursena.fenlab_android.domain.model.Step

fun StepResponse.toDomain(): Step = Step(
    id        = id,
    stepOrder = stepOrder,
    stepText  = stepText
)