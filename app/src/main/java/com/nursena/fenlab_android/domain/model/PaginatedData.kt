package com.nursena.fenlab_android.domain.model

data class PaginatedData<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val isFirst: Boolean,
    val isLast: Boolean
) {
    val hasNextPage: Boolean     get() = !isLast
    val hasPreviousPage: Boolean get() = !isFirst
}
