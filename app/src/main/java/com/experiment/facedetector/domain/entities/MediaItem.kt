package com.experiment.facedetector.domain.entities

import android.graphics.Bitmap
import android.net.Uri
import com.experiment.facedetector.data.local.entities.FaceEntity
import com.google.mlkit.vision.face.Face
import kotlinx.serialization.Serializable
import java.io.File

data class MediaItem(
    val mediaId: Long,
    val contentUri: Uri,
)

data class FaceMediaItem(
    val userImage: MediaItem,
    val faces: List<Face>,
    val thumbnail: Bitmap,
)

data class FaceMediaGridItem(
    val mediaId: Long,
    val file : File
)

data class FaceImageResult(
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