package com.experiment.facedetector.viewmodel

import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.experiment.facedetector.common.ImageHelper
import com.experiment.facedetector.common.toFaceId
import com.experiment.facedetector.core.FaceDetectionProcessor
import com.experiment.facedetector.data.local.entities.FaceEntity
import com.experiment.facedetector.domain.entities.FaceTag
import com.experiment.facedetector.domain.entities.FullImageResult
import com.experiment.facedetector.repo.MediaRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FullImageViewModel(
    savedStateHandle: SavedStateHandle,
    private val faceDetectionProcessor: FaceDetectionProcessor,
    private val imageHelper: ImageHelper,
    private val mediaRepo: MediaRepo,
) : ViewModel() {
    private val _fullImageResult = MutableStateFlow<FullImageResult?>(null)
    val fullImageResult: StateFlow<FullImageResult?> = _fullImageResult

    init {
        init(savedStateHandle)
    }

    fun init(savedStateHandle: SavedStateHandle) {
        val mediaId: Long = savedStateHandle["mediaId"] ?: error("Missing mediaId")
        viewModelScope.launch(Dispatchers.IO) {
            val mediaEntity = mediaRepo.getMedia(mediaId)
            val savedFaceTags = mediaRepo.getFaces(mediaId)
            val bitmap = imageHelper.decodeBitmap(mediaEntity.contentUri.toUri(), 1280, 720)
            val faces = faceDetectionProcessor.detectFaces(bitmap)
            val faceTags = faces.map {
                val faceId = it.toFaceId(mediaId)
                val savedFaceEntity = savedFaceTags.find { it.faceId == faceId }
                FaceTag(
                    id = faceId,
                    left = it.boundingBox.left,
                    top = it.boundingBox.top,
                    right = it.boundingBox.right,
                    bottom = it.boundingBox.bottom,
                    height = it.boundingBox.height(),
                    width = it.boundingBox.width(),
                    tag = savedFaceEntity?.tag ?: "",
                    savedFaceEntity
                )
            }
            val result = FullImageResult(mediaId, bitmap, faceTags)
            _fullImageResult.value = result
        }
    }

    fun saveFaceTag(mediaId: Long, faceTag: FaceTag, newTag: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedFaceEntity = faceTag.savedFaceEntity?.copy(tag = newTag)
                ?: FaceEntity(
                    mediaId = mediaId,
                    faceId = faceTag.id,
                    tag = newTag
                )
            mediaRepo.faceDao.insertOrUpdateFace(updatedFaceEntity)
            _fullImageResult.update { current ->
                current?.copy(
                    faces = current.faces.map {
                        if (it.id == faceTag.id) {
                            it.copy(tag = newTag, savedFaceEntity = updatedFaceEntity)
                        } else it
                    }
                )
            }
        }
    }


}



