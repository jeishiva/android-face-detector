package com.experiment.facedetector.di


import com.experiment.facedetector.repo.MediaRepo
import com.experiment.facedetector.repo.FaceMediaRepo
import org.koin.dsl.module

val repositoryModule = module {
    single {
        FaceMediaRepo(context = get(), mediaDao = get())
    }
    single {
        MediaRepo(mediaDao = get(), faceDao = get())
    }
}
