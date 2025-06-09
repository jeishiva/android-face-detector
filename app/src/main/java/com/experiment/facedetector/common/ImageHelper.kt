package com.experiment.facedetector.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
import androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180
import androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270
import androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90
import androidx.exifinterface.media.ExifInterface.ORIENTATION_UNDEFINED
import androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max

object ImageHelper {
    fun saveBitmap(
        context: Context,
        bitmap: Bitmap,
        filename: String,
        format: Bitmap.CompressFormat,
        quality: Int
    ): File? {
        val file = getThumbnailPath(context, filename)
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(format, quality, out)
                out.flush()
            }
            val sizeInKB = file.length() / 1024
            println("Saved WebP file size: $sizeInKB KB")
            return file
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun getThumbnailPath(context: Context, filename: String): File {
        return File(context.cacheDir, "$filename.webp")
    }

    fun decodeBitmap(
        context: Context,
        contentUri: Uri,
        targetHeight: Int,
        targetWidth: Int
    ): Bitmap {
        val rotationDegrees = getImageRotation(context, contentUri)
        val (originalWidth, originalHeight) = getImageDimensions(context, contentUri)

        val (adjustedWidth, adjustedHeight) = if (rotationDegrees == 90 || rotationDegrees == 270) {
            originalHeight to originalWidth
        } else {
            originalWidth to originalHeight
        }

        val sampleSize = calculateInSampleSize(
            adjustedWidth, adjustedHeight,
            targetHeight, targetWidth
        )

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val decodedBitmap = context.contentResolver.openInputStream(contentUri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        } ?: throw IllegalArgumentException("Failed to decode bitmap from URI: $contentUri")

        val resultBitmap = rotateBitmapIfNeeded(decodedBitmap, rotationDegrees)
        if (resultBitmap != decodedBitmap) {
            BitmapPool.put(decodedBitmap)
        }
        return resultBitmap
    }


    private fun createThumbnail(bitmap: Bitmap, maxSize: Int): Bitmap {
        val scale = maxSize.toFloat() / max(bitmap.width, bitmap.height)
        val width = (bitmap.width * scale).toInt()
        val height = (bitmap.height * scale).toInt()
        val thumbnail = BitmapPool.get(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(thumbnail)
        val src = Rect(0, 0, bitmap.width, bitmap.height)
        val dst = Rect(0, 0, width, height)
        canvas.drawBitmap(bitmap, src, dst, null)
        return thumbnail
    }

    private fun rotateBitmapIfNeeded(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        if (rotationDegrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        return rotatedBitmap
    }

    private fun getImageRotation(context : Context, contentUri: Uri): Int {
        context.contentResolver.openInputStream(contentUri)?.use { inputStream ->
            val exif = ExifInterface(inputStream)
            return when (exif.getAttributeInt(TAG_ORIENTATION, ORIENTATION_UNDEFINED)) {
                ORIENTATION_ROTATE_90 -> 90
                ORIENTATION_ROTATE_180 -> 180
                ORIENTATION_ROTATE_270 -> 270
                ORIENTATION_NORMAL, ORIENTATION_UNDEFINED -> 0
                else -> 0
            }
        }
        return 0
    }

    private fun getImageDimensions(context : Context, contentUri: Uri): Pair<Int, Int> {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(contentUri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
        return options.outWidth to options.outHeight
    }

    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

}

