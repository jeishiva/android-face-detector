package com.experiment.facedetector.image

import android.graphics.Bitmap
import androidx.collection.LruCache
import androidx.core.graphics.createBitmap

object BitmapPool {
    private val maxSize = (Runtime.getRuntime().maxMemory() / 8).toInt()
    private val pool = object : LruCache<String, Bitmap>(maxSize) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    fun get(width: Int, height: Int, config: Bitmap.Config): Bitmap {
        val key = "$width-$height-${config.name}"
        return pool.remove(key) ?: createBitmap(width, height, config)
    }

    fun put(bitmap: Bitmap) {
        if (!bitmap.isRecycled) {
            val key = "${bitmap.width}-${bitmap.height}-${bitmap.config?.name}"
            pool.put(key, bitmap)
        }
    }
}