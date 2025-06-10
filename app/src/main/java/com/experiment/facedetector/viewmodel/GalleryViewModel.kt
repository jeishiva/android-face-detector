package com.experiment.facedetector.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.experiment.facedetector.common.CAMERA_WORKER_TAG
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.data.local.dao.MediaDao
import com.experiment.facedetector.data.local.entities.MediaEntity
import com.experiment.facedetector.data.local.worker.CameraImageWorker
import com.experiment.facedetector.domain.entities.ProcessedMediaItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import java.io.File

class GalleryViewModel(
    private val mediaDao: MediaDao,
    private val workManager: WorkManager
) : ViewModel() {

    private val _workInfoStateFlow = MutableStateFlow<List<WorkInfo>>(emptyList())
    val workInfoStateFlow: StateFlow<List<WorkInfo>> = _workInfoStateFlow

    init {
        observeWorkStatus()
    }

    private fun observeWorkStatus() {
        workManager
            .getWorkInfosByTagLiveData(WORK_TAG)
            .asFlow()
            .onEach {
                _workInfoStateFlow.value = it
                LogManager.d("GalleryViewModel", "Work status: ${it.map { it.state }}")
            }
            .launchIn(viewModelScope)
    }

    fun startInitialWork() {
        val workRequest = OneTimeWorkRequestBuilder<CameraImageWorker>()
            .addTag(CAMERA_WORKER_TAG)
            .build()
        workManager.enqueueUniqueWork(
            CAMERA_WORKER_TAG,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    val userImageFlow: Flow<PagingData<ProcessedMediaItem>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            enablePlaceholders = true,
            initialLoadSize = INITIAL_LOAD_SIZE
        )
    ) {
        mediaDao.getPagedMedia()
    }.flow
        .map { pagingData: PagingData<MediaEntity> ->
            pagingData.map { mediaEntity ->
                ProcessedMediaItem(
                    mediaId = mediaEntity.mediaId,
                    file = File(mediaEntity.thumbnailUri)
                )
            }
        }
        .cachedIn(viewModelScope)

    companion object {
        const val PAGE_SIZE = 10
        const val INITIAL_LOAD_SIZE = 10
        private const val WORK_TAG = "camera_image_work"
    }
}


