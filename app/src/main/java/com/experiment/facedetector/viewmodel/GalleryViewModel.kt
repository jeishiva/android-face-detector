package com.experiment.facedetector.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.domain.entities.FaceImage
import com.experiment.facedetector.repo.UserImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GalleryViewModel(
    repository: UserImageRepository
) : ViewModel() {

    val userImageFlow: Flow<PagingData<FaceImage>> =
        repository.getUserImageStream(PAGE_SIZE).map { pagingData ->
            pagingData.filter { faceImage ->
                val hasFaces = faceImage.faces.isNotEmpty()
                if (!hasFaces) {
                    LogManager.d(message = "No faces detected for: ${faceImage.userImage.contentUri}")
                }
                hasFaces
            }
        }.cachedIn(viewModelScope)

    companion object {
        const val PAGE_SIZE = 10
    }
}


