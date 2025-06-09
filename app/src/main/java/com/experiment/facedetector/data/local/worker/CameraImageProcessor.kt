package com.experiment.facedetector.data.local.worker

import com.experiment.facedetector.domain.entities.FaceImage


import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.core.FaceDetectionProcessor
import com.experiment.facedetector.domain.entities.UserImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CameraImageProcessor(
    private val context: Context,
    private val pageSize: Int = 50,
    private val faceDetectionProcessor: FaceDetectionProcessor,
    private val saveCallback: suspend (faceImage: FaceImage) -> Unit,
) {

    suspend fun processAllImages() = withContext(Dispatchers.IO) {
        var page = 0
        while (true) {
            val images = queryCameraImages(pageSize, page * pageSize)
            if (images.isEmpty()) break
            for (image in images) {
                try {
                    val faceImage = faceDetectionProcessor.processImage(image)
                    saveCallback(faceImage)
                } catch (e: Exception) {
                    e.printStackTrace()
                    LogManager.e(message = "Failed processing image ${image.contentUri}: ${e.message}")
                }
            }
            page++
        }
    }

    private fun queryCameraImages(limit: Int, offset: Int): List<UserImage> {
        val images = mutableListOf<UserImage>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.RELATIVE_PATH
        )
        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("%DCIM/Camera%")
        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val queryArgs = Bundle().apply {
            putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
            putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
            putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(MediaStore.Images.Media.DATE_TAKEN))
            putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, ContentResolver.QUERY_SORT_DIRECTION_DESCENDING)
            putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
            putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
        }
        val cursor = context.contentResolver.query(
            queryUri,
            projection,
            queryArgs,
            null
        )
        cursor?.use {
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val contentUri = ContentUris.withAppendedId(queryUri, id)
                images.add(UserImage(mediaId = id, contentUri = contentUri))
                LogManager.d(message = "Camera Image: ID=$id, Path=$contentUri")
            }
        }
        return images
    }
}
