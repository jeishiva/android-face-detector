package com.experiment.facedetector.core

import com.experiment.facedetector.common.await
import com.experiment.facedetector.domain.entities.UserImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.experiment.facedetector.domain.entities.FaceImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetector
import android.graphics.Bitmap
import com.experiment.facedetector.common.ImageHelper

class FaceDetectionProcessor(
    private val faceDetector: FaceDetector,
    private val imageHelper: ImageHelper
) {
    suspend fun processImage(userImage: UserImage): FaceImage = withContext(Dispatchers.IO) {
        val bitmap = imageHelper.decodeBitmap(userImage.contentUri, MAX_HEIGHT, MAX_WIDTH)
        val faces = detectFaces(bitmap)
        FaceImage(userImage, faces, bitmap)
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
