package com.experiment.facedetector.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.experiment.facedetector.image.BitmapHelper
import com.experiment.facedetector.face.FaceDetectionProcessor
import com.experiment.facedetector.data.local.dao.MediaDao
import com.experiment.facedetector.data.local.worker.CameraImageWorker
import com.experiment.facedetector.repo.FaceMediaRepo
import org.koin.core.Koin

class KoinWorkerFactory(private val koin: Koin) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            CameraImageWorker::class.java.name -> {
                getCameraImageWorker(appContext, workerParameters)
            }
            else -> null
        }
    }
    private fun getCameraImageWorker(
        appContext: Context,
        workerParameters: WorkerParameters
    ): CameraImageWorker {
        val processor: FaceDetectionProcessor = koin.get()
        val mediaDao: MediaDao = koin.get()
        val repo: FaceMediaRepo = koin.get()
        val imageHelper: BitmapHelper = koin.get()
        return CameraImageWorker(appContext, workerParameters, processor, mediaDao, repo, imageHelper)
    }
}
