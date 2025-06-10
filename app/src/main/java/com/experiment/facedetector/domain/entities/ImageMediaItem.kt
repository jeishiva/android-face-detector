package com.experiment.facedetector.domain.entities

import android.graphics.Bitmap
import android.net.Uri
import com.experiment.facedetector.data.local.entities.FaceEntity
import com.google.mlkit.vision.face.Face
import java.io.File

/**
 *  from gallery
 */
data class ImageMediaItem(
    val mediaId: Long,
    val contentUri: Uri,
)

/**
 *  faces detected in image
 */
data class FaceDetectedItem(
    val mediaItem: ImageMediaItem,
    val faces: List<Face>,
    val thumbnail: Bitmap,
)

/**
 *  processed and locally saved
 */
data class ProcessedMediaItem(
    val mediaId: Long,
    val file : File
)

/*
 *  used in FullImageScreen
 */
data class FullImageWithFaces(
    val mediaId: Long,
    val imageContent: Bitmap,
    val faces: List<FaceTag>
)

data class FaceTag(
    val id: String,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val height: Int,
    val width: Int,
    val tag: String,
    val savedFaceEntity: FaceEntity?
)