package com.nursena.fenlab_android.data.remote.mapper

import com.nursena.fenlab_android.data.remote.dto.response.QuestionResponse
import com.nursena.fenlab_android.domain.model.Question

fun QuestionResponse.toDomain(): Question = Question(
    id           = id,
    asker        = asker.toDomain(),
    questionText = questionText,
    answerText   = answerText,
    answerer     = answerer?.toDomain(),
    createdAt    = createdAt,
    answeredAt   = answeredAt,
    isAnswered   = isAnswered,
    canAnswer    = canAnswer
)