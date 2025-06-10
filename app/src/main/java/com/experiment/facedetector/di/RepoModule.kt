package com.experiment.facedetector.di


import com.experiment.facedetector.repo.MediaRepo
import com.experiment.facedetector.repo.ProcessedMediaRepo
import org.koin.dsl.module

val repositoryModule = module {
    single {
        ProcessedMediaRepo(context = get(), mediaDao = get())
    }
    single {
        MediaRepo(mediaDao = get(), faceDao = get())
    }
}
