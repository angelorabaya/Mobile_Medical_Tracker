package com.example.medtrack.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object ImageUtils {

    // Longest edge of the downsampled image. 2048px is more than enough for on-device
    // viewing and zoom, and cuts a typical 12MP camera photo down to roughly 1/10 of
    // its original size in storage and decode cost.
    private const val MAX_IMAGE_DIMENSION = 2048
    private const val JPEG_QUALITY = 85

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

    /**
     * Copies an image from [sourceUri] into app-internal storage, downsampled and
     * re-encoded as JPEG so large camera photos don't bloat storage or slow down
     * Coil decoding. EXIF rotation is applied so images are stored upright.
     */
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

            val bitmap = decodeDownsampled(context, sourceUri) ?: return null
            val rotated = rotate(bitmap, readExifRotation(context, sourceUri))
            if (rotated !== bitmap) {
                bitmap.recycle()
            }
            FileOutputStream(destinationFile).use { outputStream ->
                rotated.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
            }
            if (!rotated.isRecycled) {
                rotated.recycle()
            }
            destinationFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    private fun decodeDownsampled(context: Context, sourceUri: Uri): Bitmap? {
        // First pass: read only the dimensions to pick a power-of-two sample size.
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(sourceUri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight)
        }
        val bitmap = context.contentResolver.openInputStream(sourceUri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return null

        // Second pass: clamp the largest edge to MAX_IMAGE_DIMENSION exactly.
        return scaleDown(bitmap, MAX_IMAGE_DIMENSION).also { scaled ->
            if (scaled !== bitmap) bitmap.recycle()
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int): Int {
        var inSampleSize = 1
        var longest = maxOf(width, height)
        while (longest > MAX_IMAGE_DIMENSION * 2) {
            inSampleSize *= 2
            longest /= 2
        }
        return inSampleSize
    }

    private fun scaleDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (maxOf(width, height) <= maxDimension) return bitmap
        val scale = maxDimension.toFloat() / maxOf(width, height)
        return Bitmap.createScaledBitmap(
            bitmap,
            (width * scale).toInt().coerceAtLeast(1),
            (height * scale).toInt().coerceAtLeast(1),
            true
        )
    }

    private fun readExifRotation(context: Context, sourceUri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(sourceUri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (_: Exception) {
            0
        }
    }

    private fun rotate(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
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
