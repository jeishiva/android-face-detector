package com.experiment.facedetector.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import androidx.exifinterface.media.ExifInterface
import androidx.exifinterface.media.ExifInterface.*
import com.experiment.facedetector.common.BitmapPool
import com.experiment.facedetector.common.await
import com.experiment.facedetector.domain.entities.UserImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.experiment.facedetector.domain.entities.FaceImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetector
import kotlin.math.max

class FaceDetectionProcessor(
    private val context: Context,
    private val faceDetector: FaceDetector
) {

    companion object {
        private const val THUMBNAIL_SIZE = 200
        private const val MAX_HEIGHT = 1280
        private const val MAX_WIDTH = 720
    }

    suspend fun processImage(userImage: UserImage): FaceImage = withContext(Dispatchers.IO) {
        val rotationDegrees = getImageRotation(userImage)
        val (originalWidth, originalHeight) = getImageDimensions(userImage)

        val (sampleWidth, sampleHeight) = when (rotationDegrees) {
            90, 270 -> originalHeight to originalWidth
            else -> originalWidth to originalHeight
        }
        val sampleSize = calculateInSampleSize(
            sampleWidth, sampleHeight,
            MAX_WIDTH, MAX_HEIGHT
        )
        val bitmap = decodeBitmap(userImage, sampleSize)
        val thumbnail = createThumbnail(bitmap, THUMBNAIL_SIZE)
        val faces = detectFaces(bitmap)

        FaceImage(userImage, faces, thumbnail)
    }

    private fun getImageRotation(userImage: UserImage): Int {
        context.contentResolver.openInputStream(userImage.contentUri)?.use { inputStream ->
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

    private fun getImageDimensions(userImage: UserImage): Pair<Int, Int> {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(userImage.contentUri)?.use { stream ->
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

    private fun decodeBitmap(userImage: UserImage, sampleSize: Int): Bitmap {
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        context.contentResolver.openInputStream(userImage.contentUri)?.use { stream ->
            return BitmapFactory.decodeStream(stream, null, options)
                ?: throw IllegalArgumentException("Bitmap decode failed")
        }
        throw IllegalArgumentException("Unable to open input stream")
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

    private suspend fun detectFaces(bitmap: Bitmap): List<Face> {
        val inputImage = InputImage.fromBitmap(bitmap, 0) // rotationDegrees = 0, per request
        return faceDetector.process(inputImage).await()
    }
}
