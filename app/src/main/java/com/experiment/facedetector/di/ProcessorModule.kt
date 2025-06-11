package com.experiment.facedetector.di

import com.experiment.facedetector.face.FaceDetectionProcessor
import com.experiment.facedetector.data.local.worker.CameraImageWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val processorModule = module {

    single {
        FaceDetectionProcessor(
            faceDetector = get(),
            imageHelper = get()
        )
    }

    worker {
        CameraImageWorker(
            context = get(),
            workerParams = get(),
            faceDetectionProcessor = get(),
            mediaRepo = get(),
            imageHelper = get()
        )
    }
}
