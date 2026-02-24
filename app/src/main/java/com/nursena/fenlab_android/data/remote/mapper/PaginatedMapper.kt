package com.nursena.fenlab_android.data.remote.mapper

import com.nursena.fenlab_android.data.remote.dto.response.PaginatedResponse
import com.nursena.fenlab_android.domain.model.PaginatedData

fun <T, R> PaginatedResponse<T>.toDomain(mapper: (T) -> R): PaginatedData<R> = PaginatedData(
    content       = content.map(mapper),
    page          = page,
    size          = size,
    totalElements = totalElements,
    totalPages    = totalPages,
    isFirst       = isFirst,
    isLast        = isLast
)