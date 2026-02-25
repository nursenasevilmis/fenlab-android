package com.nursena.fenlab_android.domain.model.enums

enum class SubjectType {
    SCIENCE,
    PHYSICS,
    CHEMISTRY,
    BIOLOGY,
    OTHER;

    fun toDisplayString(): String = when (this) {
        SCIENCE   -> "Fen Bilimleri"
        PHYSICS   -> "Fizik"
        CHEMISTRY -> "Kimya"
        BIOLOGY   -> "Biyoloji"
        OTHER     -> "Diğer"
    }
}
