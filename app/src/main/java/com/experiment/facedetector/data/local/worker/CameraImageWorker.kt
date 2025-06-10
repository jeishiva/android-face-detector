package com.experiment.facedetector.data.local.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.experiment.facedetector.image.BitmapHelper
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.face.FaceDetectionProcessor
import com.experiment.facedetector.repo.MediaRepo

class CameraImageWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val faceDetectionProcessor: FaceDetectionProcessor,
    private val mediaRepo: MediaRepo,
    private val imageHelper: BitmapHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        LogManager.d(message = "image worker started")
        val processor = CameraImageProcessor(
            context = applicationContext,
            faceDetectionProcessor = faceDetectionProcessor,
            mediaRepo = mediaRepo,
            imageHelper = imageHelper
        )
        return try {
            processor.processAllImages()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            LogManager.e(message = "CameraImageWorker failed: ${e.message}")
            Result.failure()
        }
    }
}
