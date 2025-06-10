package com.experiment.facedetector.di


import com.experiment.facedetector.repo.MediaRepo
import org.koin.dsl.module

val repositoryModule = module {
    single {
        MediaRepo(mediaDao = get(), faceDao = get())
    }
}
