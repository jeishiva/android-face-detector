package com.experiment.facedetector.domain.entities

import android.net.Uri
import java.io.File

data class UserImage(
    val mediaId: Long,
    val contentUri: Uri,
)

data class FaceImage(
    val mediaId: Long,
    val contentUri: Uri,
    var file : File? = null
)
