package com.experiment.facedetector.common

import com.experiment.facedetector.core.AppConfig

fun Long.toFileName() : String {
    return StringBuilder().apply {
        append(AppConfig.THUMBNAIL_FILE_PREFIX)
        append(this@toFileName)
    }.toString()
}