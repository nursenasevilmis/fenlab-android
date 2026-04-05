package com.nursena.fenlab_android.data.repository

import com.nursena.fenlab_android.core.base.BaseRepository
import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.core.toMinioUrl  // ← DÜZELTME: import eklendi
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
        // ── DÜZELTME ────────────────────────────────────────────────────────
        // Backend "fenlab-pdfs/xxx.pdf" şeklinde MinIO path döndürüyor.
        // DownloadManager bunu doğrudan kullanamaz; toMinioUrl() ile
        // "http://192.168.1.X:9000/fenlab-pdfs/xxx.pdf" formatına çeviriyoruz.
        val fullUrl = rawPath.toMinioUrl() ?: rawPath
        PdfDownload(pdfUrl = fullUrl)
    }

    override suspend fun downloadPdf(experimentId: Long): ApiResult<ResponseBody> = safeApiCall {
        pdfApi.downloadPdf(experimentId)
    }
}