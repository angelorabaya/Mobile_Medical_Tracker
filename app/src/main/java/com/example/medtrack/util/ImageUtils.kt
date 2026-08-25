package com.example.medtrack.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageUtils {

    fun createImageFileUri(context: Context): Pair<File, Uri> {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(context.cacheDir, "images").apply {
            if (!exists()) mkdirs()
        }
        val imageFile = File(storageDir, "JPEG_${timeStamp}_${System.currentTimeMillis()}.jpg").apply {
            if (!exists()) createNewFile()
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
        return Pair(imageFile, uri)
    }

    fun saveImageToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = File(context.filesDir, "images").apply {
                if (!exists()) mkdirs()
            }
            val destinationFile = File(storageDir, "IMG_${timeStamp}.jpg")

            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
