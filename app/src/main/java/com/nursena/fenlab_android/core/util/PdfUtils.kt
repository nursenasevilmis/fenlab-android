package com.nursena.fenlab_android.core.util

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File

object PdfUtils {

    // PDF dosyasını Downloads klasörüne kaydet yolu oluştur
    fun getPdfSavePath(context: Context, experimentId: Long): String {
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: context.filesDir
        return File(downloadsDir, "deney_$experimentId.pdf").absolutePath
    }

    // Kaydedilen PDF'i harici uygulama ile aç (PDF viewer)
    fun openPdf(context: Context, filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (!file.exists()) return false

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    // PDF URL'sini DownloadManager ile indir (alternatif yöntem)
    fun downloadPdfViaManager(context: Context, pdfUrl: String, fileName: String): Long {
        val request = DownloadManager.Request(Uri.parse(pdfUrl)).apply {
            setTitle(fileName)
            setDescription("PDF indiriliyor...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            setMimeType("application/pdf")
        }

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return manager.enqueue(request)
    }
}