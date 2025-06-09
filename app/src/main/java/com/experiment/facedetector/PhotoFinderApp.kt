package com.experiment.facedetector

import android.app.Application
import com.experiment.facedetector.di.appModule
import com.experiment.facedetector.di.databaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import androidx.work.Configuration
import org.koin.androidx.workmanager.koin.workManagerFactory

class PhotoFinderApp() : Application(),
    Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PhotoFinderApp)
            workManagerFactory()
            modules(listOf(appModule, databaseModule))
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
}
