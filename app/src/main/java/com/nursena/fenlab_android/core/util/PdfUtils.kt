package com.nursena.fenlab_android.core.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

object PdfUtils {

    fun downloadPdfViaManager(
        context: Context,
        pdfUrl: String,
        fileName: String,
        token: String?
    ): Long {
        Log.d("PdfUtils", "İndirilecek URL: $pdfUrl")

        if (pdfUrl.isBlank()) {
            Log.e("PdfUtils", "PDF URL boş!")
            return -1L
        }

        Thread {
            try {
                val client = OkHttpClient()

                val requestBuilder = Request.Builder()
                    .url(pdfUrl)
                    .addHeader("Accept", "application/pdf")

                if (!token.isNullOrBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }

                val response = client.newCall(requestBuilder.build()).execute()

                Log.d("PdfUtils", "Response code: ${response.code}")
                Log.d("PdfUtils", "Content-Type: ${response.header("Content-Type")}")

                if (!response.isSuccessful) {
                    Log.e("PdfUtils", "PDF indirilemedi. Code: ${response.code}")
                    return@Thread
                }

                val bytes = response.body?.bytes()

                if (bytes == null || bytes.isEmpty()) {
                    Log.e("PdfUtils", "PDF boş geldi!")
                    return@Thread
                }

                val header = String(bytes.take(4).toByteArray())
                Log.d("PdfUtils", "PDF header: $header")
                Log.d("PdfUtils", "PDF size: ${bytes.size} byte")

                if (header != "%PDF") {
                    Log.e("PdfUtils", "Gelen dosya PDF değil! Header: $header")
                    return@Thread
                }

                val resolver = context.contentResolver

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            Environment.DIRECTORY_DOWNLOADS
                        )
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                }

                val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Files.getContentUri("external")
                }

                val downloadUri = resolver.insert(collectionUri, values)

                if (downloadUri == null) {
                    Log.e("PdfUtils", "MediaStore URI oluşturulamadı!")
                    return@Thread
                }

                resolver.openOutputStream(downloadUri)?.use { output ->
                    output.write(bytes)
                    output.flush()
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val updateValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.IS_PENDING, 0)
                    }
                    resolver.update(downloadUri, updateValues, null, null)
                }

                Log.d("PdfUtils", "PDF Downloads klasörüne kaydedildi: $downloadUri")

                val openableCacheUri = savePdfToCacheForOpening(
                    context = context,
                    bytes = bytes,
                    fileName = fileName
                )

                Log.d("PdfUtils", "PDF bildirim için cache URI: $openableCacheUri")

                @SuppressLint("MissingPermission")
                showPdfDownloadedNotification(context, openableCacheUri, fileName)

            } catch (e: Exception) {
                Log.e("PdfUtils", "PDF indirme hatası", e)
            }
        }.start()

        return System.currentTimeMillis()
    }

    private fun savePdfToCacheForOpening(
        context: Context,
        bytes: ByteArray,
        fileName: String
    ): Uri {
        val pdfDir = File(context.cacheDir, "pdf_open")

        if (!pdfDir.exists()) {
            pdfDir.mkdirs()
        }

        val safeFileName = fileName
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .ifBlank { "FenLab_Deney.pdf" }

        val pdfFile = File(pdfDir, safeFileName)

        FileOutputStream(pdfFile).use { output ->
            output.write(bytes)
            output.flush()
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showPdfDownloadedNotification(
        context: Context,
        fileUri: Uri,
        fileName: String
    ) {
        val channelId = "pdf_download_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "PDF İndirme Bildirimleri",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            manager.createNotificationChannel(channel)
        }

        val openActivityIntent = Intent(context, PdfOpenActivity::class.java).apply {
            putExtra("pdf_uri", fileUri.toString())
            putExtra("file_name", fileName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            openActivityIntent,
            pendingIntentFlags
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("PDF indirildi")
            .setContentText(fileName)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }
}