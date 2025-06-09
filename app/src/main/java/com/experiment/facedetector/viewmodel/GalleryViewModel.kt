package com.experiment.facedetector.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.experiment.facedetector.data.local.dao.MediaDao
import com.experiment.facedetector.data.local.entities.MediaEntity
import com.experiment.facedetector.domain.entities.UIImage
import com.experiment.facedetector.repo.UserImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class GalleryViewModel(
    repository: UserImageRepository,
    mediaDao: MediaDao
) : ViewModel() {

    val userImageFlow: Flow<PagingData<UIImage>> = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = true,
            initialLoadSize = 8
        )
    ) {
        mediaDao.getPagedMedia2()
    }.flow
        .map { pagingData: PagingData<MediaEntity> ->
            pagingData.map { mediaEntity ->
                 UIImage(
                    mediaId = mediaEntity.mediaId,
                    file = File(mediaEntity.thumbnailUri)
                )
            }
        }
        .cachedIn(viewModelScope)

    companion object {
        const val PAGE_SIZE = 10
    }
}


