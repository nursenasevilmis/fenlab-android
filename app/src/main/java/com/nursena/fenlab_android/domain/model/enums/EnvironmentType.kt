package com.nursena.fenlab_android.domain.model.enums

enum class EnvironmentType {
    HOME,
    LABORATORY,
    OUTDOOR;

    fun toDisplayString(): String = when (this) {
        HOME       -> "Ev"
        LABORATORY -> "Laboratuvar"
        OUTDOOR    -> "Açık Alan"
    }
}

