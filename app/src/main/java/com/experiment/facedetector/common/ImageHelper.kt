package com.experiment.facedetector.common

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageHelper {
    fun saveBitmap(
        context: Context,
        bitmap: Bitmap,
        filename: String,
        format: Bitmap.CompressFormat,
        quality: Int
    ): File? {
        val file = File(context.cacheDir, "$filename.webp")
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(format, quality, out)
                out.flush()
            }
            val sizeInKB = file.length() / 1024
            println("Saved WebP file size: $sizeInKB KB")
            return file
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

}

