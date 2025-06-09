package com.experiment.facedetector.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.experiment.facedetector.domain.entities.UIImage
import com.experiment.facedetector.repo.UserImageRepository
import kotlinx.coroutines.flow.Flow

class GalleryViewModel(
    repository: UserImageRepository
) : ViewModel() {

    val userImageFlow: Flow<PagingData<UIImage>> =
        repository.getUserImageStream(PAGE_SIZE).cachedIn(viewModelScope)

    companion object {
        const val PAGE_SIZE = 10
    }
}


