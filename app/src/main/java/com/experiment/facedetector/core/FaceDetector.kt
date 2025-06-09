package com.experiment.facedetector.core

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Matrix
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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.foundation.Image
import com.experiment.facedetector.common.ImageHelper

class FaceDetectionProcessor(
    private val context: Context,
    private val faceDetector: FaceDetector
) {

    companion object {
        private const val MAX_HEIGHT = 1280
        private const val MAX_WIDTH = 720
    }

    suspend fun processImage(userImage: UserImage): FaceImage = withContext(Dispatchers.IO) {
        val bitmap = ImageHelper.decodeBitmap(context, userImage.contentUri, MAX_HEIGHT, MAX_WIDTH)
        val faces = detectFaces(bitmap)
        FaceImage(userImage, faces, bitmap)
    }

    private suspend fun detectFaces(bitmap: Bitmap): List<Face> {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        return faceDetector.process(inputImage).await()
    }
}
