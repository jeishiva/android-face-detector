package com.experiment.facedetector.domain.entities

import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.face.Face
import java.io.File

data class UserImage(
    val mediaId: Long,
    val contentUri: Uri,
)

data class FaceImage(
    val userImage: UserImage,
    val faces: List<Face>,
    val thumbnail: Bitmap,
)

@Serializable
data class UIImage(
    val mediaId: Long,
    val file : File
)
