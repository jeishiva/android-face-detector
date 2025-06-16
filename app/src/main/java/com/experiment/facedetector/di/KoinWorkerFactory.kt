package com.experiment.facedetector.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.experiment.facedetector.data.local.worker.CameraImageWorker
import org.koin.core.Koin

class KoinWorkerFactory(private val koin: Koin) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
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
        return CameraImageWorker(
            context = appContext,
            workerParams = workerParameters,
            processor = koin.get()
        )
    }
}
