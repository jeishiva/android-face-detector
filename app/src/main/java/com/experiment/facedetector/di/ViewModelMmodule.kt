package com.experiment.facedetector.di


import androidx.lifecycle.SavedStateHandle
import com.experiment.facedetector.viewmodel.FullImageViewModel
import com.experiment.facedetector.viewmodel.GalleryViewModel
import com.experiment.facedetector.viewmodel.SplashViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { GalleryViewModel(
        mediaDao = get(),
        workManager = get()
    ) }
    viewModel { SplashViewModel() }
    viewModel { (handle: SavedStateHandle) ->
        FullImageViewModel(
            savedStateHandle = handle,
            faceDetectionProcessor = get(),
            imageHelper = get(),
            mediaRepo = get()
        )
    }
}
