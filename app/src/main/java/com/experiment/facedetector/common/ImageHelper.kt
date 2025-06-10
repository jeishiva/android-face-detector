package com.experiment.facedetector.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
import androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180
import androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270
import androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90
import androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION
import com.google.mlkit.vision.face.Face
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max

class ImageHelper(val context: Context) {
    fun saveBitmap(
        bitmap: Bitmap,
        filename: String,
        format: Bitmap.CompressFormat,
        quality: Int
    ): File? {
        val file = getThumbnailPath(filename)
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

    fun drawFaceBoundingBoxes(
        originalBitmap: Bitmap,
        faces: List<Face>
    ): Bitmap {
        if (faces.isEmpty()) {
            return originalBitmap
        }
        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            color = Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        for (face in faces) {
            val bounds = face.boundingBox
            canvas.drawRect(bounds, paint)
        }
        return mutableBitmap
    }

    fun getThumbnailPath(filename: String): File {
        return File(context.cacheDir, "$filename.webp")
    }

    fun decodeBitmap(
        contentUri: Uri,
        targetHeight: Int,
        targetWidth: Int
    ): Bitmap {
        context.contentResolver.openInputStream(contentUri)?.let { inputStream ->
            BufferedInputStream(inputStream, 8192).use { bufferedStream ->
                bufferedStream.mark(Int.MAX_VALUE)
                val rotationDegrees = getImageRotation(bufferedStream)
                // Step 2: Get dimensions
                val (originalWidth, originalHeight) = getImageDimensions(bufferedStream)

                // Step 3: Adjust dimensions for rotation
                val (adjustedWidth, adjustedHeight) = if (rotationDegrees == 90 || rotationDegrees == 270) {
                    originalHeight to originalWidth
                } else {
                    originalWidth to originalHeight
                }

                // Step 4: Calculate inSampleSize
                val sampleSize = calculateInSampleSize(adjustedWidth, adjustedHeight, targetWidth, targetHeight)

                // Step 5: Decode bitmap
                val options = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                // Reset stream to start for decoding
                bufferedStream.reset()
                val decodedBitmap = BitmapFactory.decodeStream(bufferedStream, null, options)
                    ?: throw IllegalArgumentException("Failed to decode bitmap from URI: $contentUri")

                // Step 6: Rotate if needed
                val resultBitmap = rotateBitmapIfNeeded(decodedBitmap, rotationDegrees)
                if (resultBitmap != decodedBitmap) {
                    BitmapPool.put(decodedBitmap)
                }
                return resultBitmap
            }
        } ?: throw IOException("Unable to open input stream for URI: $contentUri")
    }

    /**
     * Retrieves the rotation angle from EXIF metadata using a provided stream.
     *
     * @param inputStream The buffered input stream (must support mark/reset).
     * @return The rotation angle in degrees (0, 90, 180, 270).
     */
    private fun getImageRotation(inputStream: BufferedInputStream): Int {
        inputStream.mark(Int.MAX_VALUE)
        return try {
            val exif = ExifInterface(inputStream)
            when (exif.getAttributeInt(TAG_ORIENTATION, ORIENTATION_NORMAL)) {
                ORIENTATION_ROTATE_90 -> 90
                ORIENTATION_ROTATE_180 -> 180
                ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } finally {
            inputStream.reset()
        }
    }

    /**
     * Retrieves image dimensions without decoding the full bitmap.
     *
     * @param inputStream The buffered input stream (must support mark/reset).
     * @return A pair of (width, height) in pixels.
     */
    private fun getImageDimensions(inputStream: BufferedInputStream): Pair<Int, Int> {
        inputStream.mark(Int.MAX_VALUE)
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.reset()
        return options.outWidth to options.outHeight
    }

    fun createThumbnail(bitmap: Bitmap, maxSize: Int): Bitmap {
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

