package com.nursena.fenlab_android.data.remote.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

interface PdfApi {
    @GET("api/pdf/{experimentId}/generate")
    suspend fun generatePdf(@Path("experimentId") id: Long): Map<String, String>

    @GET("api/pdf/{experimentId}/download")
    @Streaming
    suspend fun downloadPdf(@Path("experimentId") id: Long): ResponseBody  // okhttp3.ResponseBody
}