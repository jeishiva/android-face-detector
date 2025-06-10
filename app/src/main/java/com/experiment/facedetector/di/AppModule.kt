package com.experiment.facedetector.di

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.work.WorkManager
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.experiment.facedetector.image.BitmapHelper
import com.experiment.facedetector.core.FaceDetectionProcessor
import com.experiment.facedetector.data.local.worker.CameraImageWorker
import com.experiment.facedetector.repo.MediaRepo
import com.experiment.facedetector.repo.UserImageRepo
import com.experiment.facedetector.viewmodel.FullImageViewModel
import com.experiment.facedetector.viewmodel.GalleryViewModel
import com.experiment.facedetector.viewmodel.SplashViewModel
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module
import java.io.File
import java.util.concurrent.TimeUnit

val appModule = module {

    single {
        WorkManager.getInstance(get())
    }
    single {
        BitmapHelper(get())
    }
}




