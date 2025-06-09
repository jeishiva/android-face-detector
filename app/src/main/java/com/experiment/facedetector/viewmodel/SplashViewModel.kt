package com.experiment.facedetector.viewmodel

import androidx.lifecycle.ViewModel
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import com.experiment.facedetector.data.local.worker.CameraImageWorker

class SplashViewModel(
    private val workManager: WorkManager
) : ViewModel() {
    fun startInitialWork() {
        val workRequest = OneTimeWorkRequestBuilder<CameraImageWorker>().build()
        workManager.enqueue(workRequest)
    }
}

