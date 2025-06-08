package com.experiment.facedetector.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.experiment.facedetector.common.suspendMapPagingNotNull
import com.experiment.facedetector.domain.entities.FaceImage
import com.experiment.facedetector.domain.entities.UserImage
import com.experiment.facedetector.core.FaceDetectionProcessor
import com.experiment.facedetector.repo.UserImageRepository
import kotlinx.coroutines.flow.Flow

class GalleryViewModel(
    repository: UserImageRepository,
    private val faceDetectionProcessor: FaceDetectionProcessor
) : ViewModel() {

    val userImageFlow: Flow<PagingData<FaceImage>> = repository.getUserImageStream(PAGE_SIZE)
        .suspendMapPagingNotNull<UserImage, FaceImage>  { userImage ->
            faceDetectionProcessor.processImage(userImage)
        }
        .cachedIn(viewModelScope)

    companion object {
        const val PAGE_SIZE = 10
    }
}


