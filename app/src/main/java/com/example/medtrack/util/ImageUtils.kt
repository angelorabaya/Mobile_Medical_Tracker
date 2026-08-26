package com.example.medtrack.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

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
            val storageDir = File(context.filesDir, "images").apply {
                if (!exists()) mkdirs()
            }
            // Guaranteed-unique filename per upload. Using only a second-level
            // timestamp caused different records to share (and overwrite) the
            // same file, making every lab test show the most recently uploaded photo.
            val fileName = "IMG_${System.currentTimeMillis()}_${UUID.randomUUID().toString().substring(0, 8)}.jpg"
            val destinationFile = File(storageDir, fileName)
            android.util.Log.d("MedTrackDebug", "saveImageToInternalStorage: src=$sourceUri dest=$destinationFile")

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

    /**
     * Deletes an image file that is no longer referenced (e.g. when a record
     * is deleted or its photo is replaced), preventing orphaned files from
     * accumulating in app storage.
     */
    fun deleteImageFile(path: String?) {
        if (path.isNullOrBlank()) return
        try {
            File(path).takeIf { it.exists() }?.delete()
        } catch (_: Exception) {
            // Best effort cleanup.
        }
    }
}
