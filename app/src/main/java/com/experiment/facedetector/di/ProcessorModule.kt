package com.experiment.facedetector.di

import com.experiment.facedetector.core.FaceDetectionProcessor
import com.experiment.facedetector.data.local.worker.CameraImageWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val processorModule = module {

    single {
        FaceDetectionProcessor(get(), get())
    }

    worker {
        CameraImageWorker(get(), get(), get(), get(), get(), get())
    }
}
