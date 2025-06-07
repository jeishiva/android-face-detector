package com.experiment.facedetector

import android.app.Application
import com.experiment.facedetector.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class PhotoFinderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PhotoFinderApp)
            modules(appModule)
        }
    }
}
