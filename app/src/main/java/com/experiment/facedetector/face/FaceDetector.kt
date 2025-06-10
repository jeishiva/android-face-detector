package com.experiment.facedetector.face

import com.experiment.facedetector.common.await
import com.experiment.facedetector.domain.entities.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.experiment.facedetector.domain.entities.FaceDetectedMediaItem
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetector
import android.graphics.Bitmap
import com.experiment.facedetector.image.BitmapHelper

class FaceDetectionProcessor(
    private val faceDetector: FaceDetector,
    private val imageHelper: BitmapHelper
) {
    suspend fun processImage(userImage: MediaItem): FaceDetectedMediaItem = withContext(Dispatchers.IO) {
        val bitmap = imageHelper.decodeBitmap(userImage.contentUri, MAX_HEIGHT, MAX_WIDTH)
        val faces = detectFaces(bitmap)
        FaceDetectedMediaItem(userImage, faces, bitmap)
    }

    suspend fun detectFaces(bitmap: Bitmap): List<Face> {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        return faceDetector.process(inputImage).await()
    }

    companion object {
        private const val MAX_HEIGHT = 1280
        private const val MAX_WIDTH = 720
    }
}
