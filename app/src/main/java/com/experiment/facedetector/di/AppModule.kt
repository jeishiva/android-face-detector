package com.experiment.facedetector.di

import android.content.Context
import androidx.work.WorkManager
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.experiment.facedetector.core.FaceDetectionProcessor
import com.experiment.facedetector.data.local.worker.CameraImageWorker
import com.experiment.facedetector.repo.UserImageRepository
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
        UserImageRepository(get(), get())
    }
    single {
       provideImageLoader(context = get(), okHttpClient = get())
    }
    single {
        WorkManager.getInstance(get())
    }
    single {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", "FaceDetector/1.0") // Custom header
                    .build()
                chain.proceed(request)
            }
            .build()
    }
    single {
        provideFaceDetector()
    }
    single {
        FaceDetectionProcessor(get(), get())
    }
    viewModel { GalleryViewModel(get(), get(), get()) }
    viewModel { SplashViewModel() }
    viewModel { FullImageViewModel() }
    worker {
        CameraImageWorker(get(), get(), get(), get(), get())
    }
}

fun provideFaceDetector(): FaceDetector {
    val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .build()
    return FaceDetection.getClient(options)
}

fun provideImageLoader(context: Context, okHttpClient: OkHttpClient): ImageLoader {
    return ImageLoader.Builder(context)
        .memoryCache {
            MemoryCache.Builder(context)
                .maxSizePercent(0.35)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(File(context.cacheDir, "image_cache"))
                .maxSizeBytes(125L * 1024 * 1024)
                .build()
        }
        .okHttpClient(okHttpClient)
        .build()
}


