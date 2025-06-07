package com.experiment.facedetector.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.experiment.facedetector.domain.entities.UserImage
import com.experiment.facedetector.repo.UserImageRepository
import kotlinx.coroutines.flow.Flow

class GalleryViewModel(repository: UserImageRepository) : ViewModel() {

    val userImageFlow: Flow<PagingData<UserImage>> = repository.getUserImageStream(30)
        .cachedIn(viewModelScope)
}