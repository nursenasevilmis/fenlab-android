package com.nursena.fenlab_android.domain.model.enums

enum class DifficultyLevel {
    EASY, MEDIUM, HARD;

    fun toDisplayString(): String = when (this) {
        EASY   -> "Kolay"
        MEDIUM -> "Orta"
        HARD   -> "Zor"
    }
}