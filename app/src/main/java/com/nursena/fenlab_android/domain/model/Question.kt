package com.nursena.fenlab_android.domain.model

data class Question(
    val id: Long,
    val asker: UserSummary,
    val questionText: String,
    val answerText: String?,
    val answerer: UserSummary?,
    val createdAt: String,
    val answeredAt: String?,
    val isAnswered: Boolean,
    val canAnswer: Boolean
)