package com.nursena.fenlab_android.domain.repository

import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.domain.model.PdfDownload
import okhttp3.ResponseBody

interface PdfRepository {

    suspend fun generatePdf(experimentId: Long): ApiResult<PdfDownload>

    suspend fun downloadPdf(experimentId: Long): ApiResult<ResponseBody>
}
