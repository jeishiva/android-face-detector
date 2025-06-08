package com.experiment.facedetector.core

import android.content.Context
import com.experiment.facedetector.domain.entities.UserImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.experiment.facedetector.domain.entities.FaceImage
import com.google.mlkit.vision.face.FaceDetector

class FaceDetectionProcessor(private val context: Context, private val faceDetector: FaceDetector) {

    suspend fun processImage(userImage: UserImage): FaceImage = withContext(Dispatchers.Default) {
        FaceImage(userImage.mediaId, userImage.contentUri, null)
    }
}
