package com.experiment.facedetector.domain.entities

import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.face.Face

data class UserImage(
    val mediaId: Long,
    val contentUri: Uri,
)

data class FaceImage(
    val userImage: UserImage,
    val faces: List<Face>,
    val thumbnail: Bitmap,
)
