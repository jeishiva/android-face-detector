package com.experiment.facedetector.viewmodel

import android.graphics.Bitmap
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.experiment.facedetector.common.ImageHelper
import com.experiment.facedetector.core.FaceDetectionProcessor
import com.experiment.facedetector.data.local.entities.MediaEntity
import com.experiment.facedetector.repo.MediaRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FullImageViewModel(
    savedStateHandle: SavedStateHandle,
    private val faceDetectionProcessor: FaceDetectionProcessor,
    private val imageHelper: ImageHelper,
    private val mediaRepo: MediaRepo,
) : ViewModel() {
    private val _media = MutableStateFlow<Bitmap?>(null)
    val media: StateFlow<Bitmap?> = _media

    init {
        val mediaId: Long = savedStateHandle["mediaId"] ?: error("Missing mediaId")
        viewModelScope.launch(Dispatchers.IO) {
            val mediaEntity = mediaRepo.getMedia(mediaId)
            val bitmap = imageHelper.decodeBitmap(mediaEntity.contentUri.toUri(), 1280, 720)
            val faces = faceDetectionProcessor.detectFaces(bitmap)
            val result = imageHelper.drawFaceBoundingBoxes(bitmap, faces)
            _media.value = result
        }
    }
}

