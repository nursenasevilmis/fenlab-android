package com.nursena.fenlab_android.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

object FileUtils {

    private const val MAX_IMAGE_DIMENSION = 1080
    private const val JPEG_QUALITY        = 80
    private const val MAX_FILE_SIZE_BYTES = 800_000

    fun uriToCompressedFile(context: Context, uri: Uri): File? {
        return try {
            val mimeType = getMimeType(context, uri)
            if (!mimeType.startsWith("image/")) {
                uriToFile(context, uri)
            } else {
                val input = context.contentResolver.openInputStream(uri) ?: return null
                val original = BitmapFactory.decodeStream(input)
                input.close()

                if (original == null) return null

                val (newW, newH) = scaleDimensions(original.width, original.height, MAX_IMAGE_DIMENSION)
                val scaled = if (newW < original.width) {
                    Bitmap.createScaledBitmap(original, newW, newH, true)
                } else original

                val outFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
                var quality = JPEG_QUALITY
                do {
                    FileOutputStream(outFile).use { out ->
                        scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
                    }
                    quality -= 10
                } while (outFile.length() > MAX_FILE_SIZE_BYTES && quality > 30)

                if (scaled != original) scaled.recycle()
                original.recycle()

                outFile
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun scaleDimensions(w: Int, h: Int, maxDim: Int): Pair<Int, Int> {
        if (w <= maxDim && h <= maxDim) return w to h
        return if (w > h) maxDim to (h * maxDim / w)
        else (w * maxDim / h) to maxDim
    }

    fun uriToFile(context: Context, uri: Uri): File? = try {
        val fileName = getFileName(context, uri) ?: "upload_${System.currentTimeMillis()}"
        val tempFile = File(context.cacheDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output -> input.copyTo(output) }
        }
        tempFile
    } catch (e: Exception) { null }

    fun fileToMultipart(file: File, mimeType: String, formFieldName: String = "file"): MultipartBody.Part {
        val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(formFieldName, file.name, requestBody)
    }

    fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && index >= 0) name = cursor.getString(index)
        }
        return name
    }

    fun getMimeType(context: Context, uri: Uri): String {
        return context.contentResolver.getType(uri)
            ?: MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()))
            ?: "application/octet-stream"
    }

    fun formatFileSize(bytes: Long): String = when {
        bytes < 1024        -> "$bytes B"
        bytes < 1024 * 1024 -> "${"%.1f".format(bytes / 1024.0)} KB"
        else                -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
    }

    fun isVideo(mimeType: String) = mimeType.startsWith("video/")
    fun isImage(mimeType: String) = mimeType.startsWith("image/")
}