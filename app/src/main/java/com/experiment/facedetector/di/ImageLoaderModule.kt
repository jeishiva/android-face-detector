package com.experiment.facedetector.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import okhttp3.OkHttpClient
import org.koin.dsl.module
import java.io.File

val imageLoaderModule = module {
    single { provideImageLoader(context = get(), okHttpClient = get()) }
}

private fun provideImageLoader(context: Context, okHttpClient: OkHttpClient): ImageLoader {
    return ImageLoader.Builder(context)
        .memoryCache {
            MemoryCache.Builder(context)
                .maxSizePercent(0.35) // 35% of available memory
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(File(context.cacheDir, "image_cache"))
                .maxSizeBytes(125L * 1024 * 1024) // 125MB
                .build()
        }
        .okHttpClient(okHttpClient)
        .build()
}
