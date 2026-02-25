package com.nursena.fenlab_android.core.network



data class NetworkException(
    val code: Int,
    override val message: String
) : Exception(message)