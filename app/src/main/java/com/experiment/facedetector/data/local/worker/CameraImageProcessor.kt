package com.experiment.facedetector.data.local.worker

import com.experiment.facedetector.domain.entities.FaceImage


import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import com.experiment.facedetector.common.ImageHelper
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.core.AppConfig
import com.experiment.facedetector.core.FaceDetectionProcessor
import com.experiment.facedetector.data.local.dao.MediaDao
import com.experiment.facedetector.data.local.entities.MediaEntity
import com.experiment.facedetector.domain.entities.UserImage
import com.experiment.facedetector.repo.UserImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CameraImageProcessor(
    private val context: Context,
    private val pageSize: Int = 5,
    private val faceDetectionProcessor: FaceDetectionProcessor,
    private val mediaDao: MediaDao,
    private val userImageRepository: UserImageRepository,
) {

    suspend fun processAllImages() = withContext(Dispatchers.IO) {
        var page = 0
        while (true) {
            val images = queryCameraImages(pageSize, page * pageSize)
            val allIds = images.map { it.mediaId }
            val existingIds = mediaDao.getExistingMediaIds(allIds)
            val missingImages = images.filterNot { it.mediaId in existingIds }
            val mediaEntityList = mutableListOf<MediaEntity>()
            for (image in missingImages) {
                try {
                    val faceImage = faceDetectionProcessor.processImage(image)
                    saveFaceImageAndThumbnail(faceImage)?.let {
                        mediaEntityList.add(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    LogManager.e(message = "Failed processing image ${image.contentUri}: ${e.message}")
                }
            }
            if (mediaEntityList.isNotEmpty()) {
                mediaDao.insertMediaList(mediaEntityList)
            }
            page++
        }
    }

    private suspend fun saveFaceImageAndThumbnail(faceImage: FaceImage): MediaEntity? {
        try {
            LogManager.d(message = "work save face image called")
            val file = ImageHelper.saveBitmap(
                context,
                faceImage.thumbnail,
                "${AppConfig.THUMBNAIL_FILE_PREFIX}${faceImage.userImage.mediaId}",
                Bitmap.CompressFormat.WEBP,
                70
            )
            if (file != null) {
                val media = MediaEntity(
                    mediaId = faceImage.userImage.mediaId,
                    contentUri = faceImage.userImage.contentUri.toString(),
                    thumbnailUri = file.absolutePath
                )
                LogManager.d(message = "inserted $media")
                return media
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
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
