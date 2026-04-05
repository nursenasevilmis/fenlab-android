package com.nursena.fenlab_android.core.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log

object PdfUtils {

    // ── DÜZELTME ────────────────────────────────────────────────────────────
    // Eski fixUrl() kaldırıldı. URL artık PdfRepositoryImpl içinde
    // toMinioUrl() ile doğru şekilde inşa ediliyor (Constants.MINIO_URL + path).
    // Burada IP replace etmeye gerek yok.
    // ────────────────────────────────────────────────────────────────────────

    fun downloadPdfViaManager(context: Context, pdfUrl: String, fileName: String, token: String?): Long {
        return try {
            Log.d("PdfUtils", "İndirilecek URL: $pdfUrl")

            if (pdfUrl.isBlank()) {
                Log.e("PdfUtils", "PDF URL boş!")
                return -1L
            }

            val uri = Uri.parse(pdfUrl)

            val request = DownloadManager.Request(uri).apply {
                setTitle(fileName)
                setDescription("PDF indiriliyor...")
                setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
                setDestinationInExternalFilesDir(
                    context,
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName
                )
                setMimeType("application/pdf")
                addRequestHeader("Accept", "application/pdf")
            }

            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)

        } catch (e: Exception) {
            Log.e("PdfUtils", "PDF indirme hatası: ${e.message}", e)
            -1L
        }
    }
}