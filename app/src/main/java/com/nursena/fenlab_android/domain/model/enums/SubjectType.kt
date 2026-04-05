package com.nursena.fenlab_android.domain.model.enums

enum class SubjectType {
    SCIENCE,
    PHYSICS,
    CHEMISTRY,
    BIOLOGY,
    MATH,
    OTHER;

    fun toDisplayString(): String = when (this) {
        SCIENCE   -> "Fen Bilimleri"
        PHYSICS   -> "Fizik"
        CHEMISTRY -> "Kimya"
        BIOLOGY   -> "Biyoloji"
        MATH      -> "Matematik"
        OTHER     -> "Diğer"
    }
}
