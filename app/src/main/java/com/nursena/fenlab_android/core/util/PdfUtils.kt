package com.nursena.fenlab_android.core.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipData
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
import okhttp3.OkHttpClient
import okhttp3.Request

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

                val fileUri = resolver.insert(collectionUri, values)

                if (fileUri == null) {
                    Log.e("PdfUtils", "MediaStore URI oluşturulamadı!")
                    return@Thread
                }

                resolver.openOutputStream(fileUri)?.use { output ->
                    output.write(bytes)
                    output.flush()
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val updateValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.IS_PENDING, 0)
                    }
                    resolver.update(fileUri, updateValues, null, null)
                }

                Log.d("PdfUtils", "PDF başarıyla kaydedildi: $fileUri")

                @SuppressLint("MissingPermission")
                showPdfDownloadedNotification(context, fileUri, fileName)

            } catch (e: Exception) {
                Log.e("PdfUtils", "PDF indirme hatası", e)
            }
        }.start()

        return System.currentTimeMillis()
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

        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "application/pdf")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION

            clipData = ClipData.newUri(
                context.contentResolver,
                fileName,
                fileUri
            )
        }

        context.packageManager.queryIntentActivities(openIntent, 0).forEach { resolveInfo ->
            context.grantUriPermission(
                resolveInfo.activityInfo.packageName,
                fileUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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