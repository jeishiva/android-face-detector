package com.experiment.facedetector.data.local.worker

import android.content.Context
import android.graphics.Bitmap
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.experiment.facedetector.common.ImageHelper
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.core.AppConfig
import com.experiment.facedetector.core.FaceDetectionProcessor
import com.experiment.facedetector.data.local.dao.MediaDao
import com.experiment.facedetector.data.local.entities.MediaEntity
import com.experiment.facedetector.domain.entities.FaceImage
import com.experiment.facedetector.repo.UserImageRepository

class CameraImageWorker(
    private val context: Context,
    workerParams: WorkerParameters,
    private val faceDetectionProcessor: FaceDetectionProcessor,
    private val mediaDao: MediaDao,
    private val userImageRepository: UserImageRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        LogManager.d(message = "do work")
        val processor = CameraImageProcessor(
            context = applicationContext,
            faceDetectionProcessor = faceDetectionProcessor,
            mediaDao = mediaDao,
            userImageRepository = userImageRepository,
            saveCallback = { faceImage ->
                saveFaceImageAndThumbnail(faceImage)
            })
        return try {
            processor.processAllImages()
            Result.success()
        } catch (e: Exception) {
            LogManager.e(message = "CameraImageWorker failed: ${e.message}")
            Result.failure()
        }
    }

    private suspend fun saveFaceImageAndThumbnail(faceImage: FaceImage) {
        try {
            LogManager.d(message = "work save face image called")
            val file = ImageHelper.saveBitmap(
                context,
                faceImage.thumbnail,
                "${AppConfig.THUMBNAIL_FILE_PREFIX}${faceImage.userImage.mediaId}",
                Bitmap.CompressFormat.WEBP,
                70
            )
            if (file != null) {
                val media =  MediaEntity(
                    mediaId = faceImage.userImage.mediaId,
                    contentUri = faceImage.userImage.contentUri.toString(),
                    thumbnailUri = file.absolutePath
                )
                mediaDao.insertMedia(media)
                LogManager.d(message = "inserted $media")
            }
        } catch (ex: Exception) {
           ex.printStackTrace()
        }
    }
}
