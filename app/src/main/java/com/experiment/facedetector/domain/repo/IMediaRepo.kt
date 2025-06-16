package com.experiment.facedetector.domain.repo

import androidx.paging.PagingData
import com.experiment.facedetector.data.local.entities.FaceEntity
import com.experiment.facedetector.data.local.entities.MediaEntity
import com.experiment.facedetector.domain.entities.ProcessedMediaItem
import kotlinx.coroutines.flow.Flow

interface IMediaRepo {
    suspend fun getMedia(mediaId: Long): MediaEntity
    suspend fun insertOrUpdateMedia(mediaList: List<MediaEntity>)
    suspend fun getExistingMediaIds(mediaIds: List<Long>): List<Long>
    fun getPagedMedia(): Flow<PagingData<ProcessedMediaItem>>
    suspend fun getFaces(mediaId: Long): List<FaceEntity>
    suspend fun insertOrUpdateFace(faceEntity: FaceEntity)
}