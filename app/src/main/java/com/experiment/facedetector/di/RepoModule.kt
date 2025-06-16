package com.experiment.facedetector.di


import com.experiment.facedetector.domain.repo.IMediaRepo
import com.experiment.facedetector.data.local.repo.MediaRepo
import org.koin.dsl.module

val repositoryModule = module {
    single<IMediaRepo> {
        MediaRepo(mediaDao = get(), faceDao = get())
    }
}
