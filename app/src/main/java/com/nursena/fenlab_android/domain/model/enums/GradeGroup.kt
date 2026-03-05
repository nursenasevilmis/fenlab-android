package com.nursena.fenlab_android.domain.model.enums

/**
 * Türk eğitim sistemine göre sınıf grupları.
 * Backend'e minGrade gönderilir — backend >= minGrade AND <= maxGrade filtreler.
 */
enum class GradeGroup(
    val minGrade: Int,
    val maxGrade: Int,
    val displayName: String
) {
    ILKOKUL(1,  4,  "İlkokul (1-4)"),
    ORTAOKUL(5, 8,  "Ortaokul (5-8)"),
    LISE(9,     12, "Lise (9-12)");

    fun toDisplayString() = displayName
}