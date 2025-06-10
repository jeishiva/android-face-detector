package com.experiment.facedetector.config

import android.graphics.Bitmap

object AppConfig {

    // thumbnail configs
    const val THUMBNAIL_FILE_PREFIX = "thumb_"
    const val THUMBNAIL_SIZE = 200
    const val THUMBNAIL_QUALITY = 80
    // using jpeg for wider compatibility
    val THUMBNAIL_FORMAT = Bitmap.CompressFormat.JPEG

}