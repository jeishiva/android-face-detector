package com.experiment.facedetector.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.experiment.facedetector.repo.UserImageRepository
import com.experiment.facedetector.viewmodel.GalleryViewModel
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.io.File
import java.util.concurrent.TimeUnit

val appModule = module {
    factory { provideRepository(get()) }
    viewModel {
        GalleryViewModel(get())
    }
    single {
       provideImageLoader(context = get(), okHttpClient = get())
    }
    single {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Connection timeout
            .readTimeout(30, TimeUnit.SECONDS)    // Read timeout
            .writeTimeout(30, TimeUnit.SECONDS)   // Write timeout
            .retryOnConnectionFailure(true)       // Retry on connection failure
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", "FaceDetector/1.0") // Custom header
                    .build()
                chain.proceed(request)
            }
            .build()
    }
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

fun provideRepository(context: Context): UserImageRepository {
    return UserImageRepository(context)
}
