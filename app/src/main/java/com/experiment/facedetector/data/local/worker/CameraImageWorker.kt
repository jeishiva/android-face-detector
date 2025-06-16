package com.experiment.facedetector.data.local.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.experiment.facedetector.image.BitmapHelper
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.data.local.worker.processor.ICameraProcessor
import com.experiment.facedetector.data.local.worker.processor.IProcessor
import com.experiment.facedetector.domain.repo.IMediaRepo
import com.experiment.facedetector.face.FaceDetectionProcessor

class CameraImageWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val processor: ICameraProcessor,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        LogManager.d(message = "image worker started")
        return try {
            processor.process()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            LogManager.e(message = "CameraImageWorker failed: ${e.message}")
            Result.failure()
        }
    }
}
