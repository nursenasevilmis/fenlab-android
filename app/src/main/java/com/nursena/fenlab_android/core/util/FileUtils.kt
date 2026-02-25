package com.nursena.fenlab_android.core.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

object FileUtils {

    // Uri → geçici File (Retrofit upload için)
    fun uriToFile(context: Context, uri: Uri): File? = try {
        val fileName = getFileName(context, uri) ?: "upload_${System.currentTimeMillis()}"
        val tempFile = File(context.cacheDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        tempFile
    } catch (e: Exception) { null }

    // File → MultipartBody.Part (Retrofit @Part için)
    // formFieldName: backend @RequestParam adı — "file"
    fun fileToMultipart(file: File, mimeType: String, formFieldName: String = "file"): MultipartBody.Part {
        val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(formFieldName, file.name, requestBody)
    }

    // Uri'den dosya adını al
    fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && index >= 0) {
                name = cursor.getString(index)
            }
        }
        return name
    }

    // Uri'den MIME type al
    fun getMimeType(context: Context, uri: Uri): String {
        return context.contentResolver.getType(uri)
            ?: MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(
                    MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                )
            ?: "application/octet-stream"
    }

    // Dosya boyutunu okunabilir formata çevir
    fun formatFileSize(bytes: Long): String = when {
        bytes < 1024        -> "$bytes B"
        bytes < 1024 * 1024 -> "${"%.1f".format(bytes / 1024.0)} KB"
        else                -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
    }

    // Video mu resim mi?
    fun isVideo(mimeType: String) = mimeType.startsWith("video/")
    fun isImage(mimeType: String) = mimeType.startsWith("image/")
}