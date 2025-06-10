package com.experiment.facedetector.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.experiment.facedetector.data.local.dao.FaceDao
import com.experiment.facedetector.data.local.dao.MediaDao
import com.experiment.facedetector.data.local.entities.FaceEntity
import com.experiment.facedetector.data.local.entities.MediaEntity
import com.experiment.facedetector.domain.entities.ProcessedMediaItem
import com.experiment.facedetector.viewmodel.GalleryViewModel.Companion.INITIAL_LOAD_SIZE
import com.experiment.facedetector.viewmodel.GalleryViewModel.Companion.PAGE_SIZE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.File

class MediaRepo(
    val mediaDao: MediaDao, val faceDao: FaceDao
) {

    suspend fun getMedia(mediaId: Long): MediaEntity {
        return mediaDao.getMediaEntityById(mediaId)
    }

    suspend fun getFaces(mediaId: Long): List<FaceEntity> {
        return faceDao.getFacesForMedia(mediaId)
    }

    fun getPagedMedia(): Flow<PagingData<ProcessedMediaItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = true,
                initialLoadSize = INITIAL_LOAD_SIZE
            )
        ) {
            mediaDao.getPagedMedia()
        }.flow.map { pagingData: PagingData<MediaEntity> ->
                pagingData.map { mediaEntity ->
                    ProcessedMediaItem(
                        mediaId = mediaEntity.mediaId,
                        file = File(mediaEntity.thumbnailUri)
                    )
                }
            }.flowOn(Dispatchers.IO)
    }
}

