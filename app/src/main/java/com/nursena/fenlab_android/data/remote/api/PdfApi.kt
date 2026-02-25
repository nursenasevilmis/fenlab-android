package com.nursena.fenlab_android.data.remote.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

interface PdfApi {

    // PDF URL döner: {"pdfUrl": "https://..."}
    @GET("api/pdf/{experimentId}/generate")
    suspend fun generatePdf(
        @Path("experimentId") experimentId: Long
    ): Map<String, String>

    // PDF binary döner — @Streaming ile büyük dosyalar için
    @Streaming
    @GET("api/pdf/{experimentId}/download")
    suspend fun downloadPdf(
        @Path("experimentId") experimentId: Long
    ): ResponseBody
}