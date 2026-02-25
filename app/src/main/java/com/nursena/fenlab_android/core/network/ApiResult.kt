package com.nursena.fenlab_android.core.network


sealed class ApiResult<out T> {

    data class Success<T>(val data: T)              : ApiResult<T>()

    data class Error(
        val message: String,
        val code: Int? = null
    )                                               : ApiResult<Nothing>()

    object Loading                                  : ApiResult<Nothing>()
}

// ── Kısayollar ────────────────────────────────────────────────────────────
val <T> ApiResult<T>.isSuccess: Boolean  get() = this is ApiResult.Success
val <T> ApiResult<T>.isError: Boolean    get() = this is ApiResult.Error
val <T> ApiResult<T>.isLoading: Boolean  get() = this is ApiResult.Loading

fun <T> ApiResult<T>.getOrNull(): T? = (this as? ApiResult.Success)?.data
