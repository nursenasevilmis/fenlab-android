package com.nursena.fenlab_android.data.repository

import com.nursena.fenlab_android.core.Constants
import com.nursena.fenlab_android.core.base.BaseRepository
import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.data.remote.api.PdfApi
import com.nursena.fenlab_android.domain.model.PdfDownload
import com.nursena.fenlab_android.domain.repository.PdfRepository
import okhttp3.ResponseBody
import javax.inject.Inject

class PdfRepositoryImpl @Inject constructor(
    private val pdfApi: PdfApi
) : BaseRepository(), PdfRepository {

    override suspend fun generatePdf(experimentId: Long): ApiResult<PdfDownload> = safeApiCall {
        val rawPath = pdfApi.generatePdf(experimentId)["pdfUrl"] ?: ""

        val fullUrl = rawPath.toPdfDownloadUrl()

        PdfDownload(pdfUrl = fullUrl)
    }

    override suspend fun downloadPdf(experimentId: Long): ApiResult<ResponseBody> = safeApiCall {
        pdfApi.downloadPdf(experimentId)
    }

    private fun String.toPdfDownloadUrl(): String {
        val value = this.trim()

        if (value.isBlank()) return value

        val bucket = "fenlab-pdfs/"
        val index = value.indexOf(bucket)

        val path = if (index != -1) {
            value.substring(index)
        } else {
            value.trimStart('/')
        }

        return "${Constants.MEDIA_BASE_URL}/${path.trimStart('/')}"
    }
}