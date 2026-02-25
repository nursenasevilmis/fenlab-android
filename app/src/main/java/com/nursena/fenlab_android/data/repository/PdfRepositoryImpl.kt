package com.nursena.fenlab_android.data.repository


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
        val pdfUrl = pdfApi.generatePdf(experimentId)["pdfUrl"] ?: ""
        PdfDownload(pdfUrl = pdfUrl)
    }

    override suspend fun downloadPdf(experimentId: Long): ApiResult<ResponseBody> = safeApiCall {
        pdfApi.downloadPdf(experimentId)
    }
}
