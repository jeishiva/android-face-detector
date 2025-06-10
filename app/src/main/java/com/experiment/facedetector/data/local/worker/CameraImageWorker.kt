package com.experiment.facedetector.data.local.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.experiment.facedetector.image.BitmapHelper
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.core.FaceDetectionProcessor
import com.experiment.facedetector.data.local.dao.MediaDao
import com.experiment.facedetector.repo.UserImageRepo

class CameraImageWorker(
    private val context: Context,
    workerParams: WorkerParameters,
    private val faceDetectionProcessor: FaceDetectionProcessor,
    private val mediaDao: MediaDao,
    private val userImageRepository: UserImageRepo,
    private val imageHelper: BitmapHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        LogManager.d(message = "do work")
        val processor = CameraImageProcessor(
            context = applicationContext,
            faceDetectionProcessor = faceDetectionProcessor,
            mediaDao = mediaDao,
            userImageRepository = userImageRepository,
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
