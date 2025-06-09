package com.experiment.facedetector.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.experiment.facedetector.data.local.entities.MediaEntity
import com.experiment.facedetector.repo.MediaRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FullImageViewModel(
    private val mediaRepo: MediaRepo,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _media = MutableStateFlow<MediaEntity?>(null)
    val media: StateFlow<MediaEntity?> = _media

    init {
        val mediaId: Long = savedStateHandle["mediaId"] ?: error("Missing mediaId")
        viewModelScope.launch(Dispatchers.IO) {
            _media.value = mediaRepo.getMedia(mediaId)
        }
    }
}

