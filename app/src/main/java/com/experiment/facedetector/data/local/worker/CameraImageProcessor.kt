package com.experiment.facedetector.data.local.worker

import com.experiment.facedetector.domain.entities.FaceDetectedMediaItem


import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import com.experiment.facedetector.image.BitmapPool
import com.experiment.facedetector.image.BitmapHelper
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.common.toFileName
import com.experiment.facedetector.face.FaceDetectionProcessor
import com.experiment.facedetector.data.local.dao.MediaDao
import com.experiment.facedetector.data.local.entities.MediaEntity
import com.experiment.facedetector.domain.entities.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.experiment.facedetector.config.ThumbnailConfig
import com.experiment.facedetector.config.ThumbnailConfig.THUMBNAIL_SIZE

/**
 *  queries camera images in batches from local storage
 *  detects faces, then creates thumbnail
 *  post the above processing stored in db
 *  and thumbnails are cached in cache dir
 *
 */
class CameraImageProcessor(
    private val context: Context,
    private val pageSize: Int = 5,
    private val faceDetectionProcessor: FaceDetectionProcessor,
    private val mediaDao: MediaDao,
    private val imageHelper: BitmapHelper,
) {

    suspend fun processAllImages() = withContext(Dispatchers.IO) {
        LogManager.d("CameraImageProcessor", "Starting image processing")
        var page = 0
        var totalProcessed = 0
        var totalSaved = 0
        while (true) {
            val images = queryCameraImages(pageSize, page * pageSize)
            if (images.isEmpty()) {
                LogManager.d(
                    "CameraImageProcessor",
                    "Completed processing. Total: $totalProcessed, Saved: $totalSaved"
                )
                break
            }
            val pageResult = processImageBatch(images)
            totalProcessed += pageResult.processed
            totalSaved += pageResult.saved
            LogManager.d(
                "CameraImageProcessor",
                "Page $page: ${pageResult.processed} processed, ${pageResult.saved} saved"
            )
            page++
        }
    }

    private data class BatchResult(val processed: Int, val saved: Int)

    private suspend fun processImageBatch(images: List<MediaItem>): BatchResult {
        // Filter out already processed images
        val imagesToProcess = filterNewImages(images)
        if (imagesToProcess.isEmpty()) {
            return BatchResult(0, 0)
        }

        val mediaEntities = mutableListOf<MediaEntity>()
        var processedCount = 0

        for (image in imagesToProcess) {
            val mediaEntity = processImageSafely(image)
            mediaEntity?.let { mediaEntities.add(it) }
            processedCount++
        }

        // Batch insert all entities
        if (mediaEntities.isNotEmpty()) {
            mediaDao.insertMediaList(mediaEntities)
        }

        return BatchResult(processedCount, mediaEntities.size)
    }

    private suspend fun filterNewImages(images: List<MediaItem>): List<MediaItem> {
        val allIds = images.map { it.mediaId }
        val existingIds = mediaDao.getExistingMediaIds(allIds)
        return images.filterNot { it.mediaId in existingIds }
    }

    private suspend fun processImageSafely(image: MediaItem): MediaEntity? {
        return try {
            processImage(image)
        } catch (e: Exception) {
            LogManager.e(
                "CameraImageProcessor",
                "Failed processing image ${image.contentUri}: ${e.message}",
                e
            )
            null
        }
    }

    private suspend fun processImage(image: MediaItem): MediaEntity? {
        val faceDetectedMediaItem = faceDetectionProcessor.processImage(image)
        if (faceDetectedMediaItem.faces.isEmpty()) {
            BitmapPool.put(faceDetectedMediaItem.image)
            return null
        }
        return createMediaEntityWithBoundingBoxes(faceDetectedMediaItem)
    }

    private suspend fun createMediaEntityWithBoundingBoxes(faceImage: FaceDetectedMediaItem): MediaEntity? {
        var boundingBoxBitmap: Bitmap? = null
        var thumbnailBitmap: Bitmap? = null

        return try {
            // Draw bounding boxes
            boundingBoxBitmap = imageHelper.drawFaceBoundingBoxes(
                faceImage.image,
                faceImage.faces
            )

            // Create thumbnail
            thumbnailBitmap =
                imageHelper.scaleFromPool(
                    boundingBoxBitmap,
                    THUMBNAIL_SIZE, THUMBNAIL_SIZE
                )

            // Save and create entity
            val updatedFaceImage = faceImage.copy(image = thumbnailBitmap)
            saveThumbnail(updatedFaceImage)
        } catch (e: Exception) {
            LogManager.e(
                "CameraImageProcessor",
                "Failed to create media entity with bounding boxes",
                e
            )
            null
        } finally {
            // Clean all bitmaps
            cleanupBitmaps(faceImage.image, boundingBoxBitmap, thumbnailBitmap)
        }
    }

    private fun cleanupBitmaps(vararg bitmaps: Bitmap?) {
        bitmaps.filterNotNull().forEach { bitmap ->
            BitmapPool.put(bitmap)
        }
    }

    private fun saveThumbnail(faceImage: FaceDetectedMediaItem): MediaEntity? {
        try {
            LogManager.d(message = "work save face image called")
            val file = imageHelper.saveBitmap(
                faceImage.image,
                faceImage.mediaItem.mediaId.toFileName(),
                ThumbnailConfig.THUMBNAIL_FORMAT,
                ThumbnailConfig.THUMBNAIL_QUALITY
            )
            if (file != null) {
                val media = MediaEntity(
                    mediaId = faceImage.mediaItem.mediaId,
                    contentUri = faceImage.mediaItem.contentUri.toString(),
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

    private fun queryCameraImages(limit: Int, offset: Int): List<MediaItem> {
        val images = mutableListOf<MediaItem>()
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
            putStringArray(
                ContentResolver.QUERY_ARG_SORT_COLUMNS,
                arrayOf(MediaStore.Images.Media.DATE_TAKEN)
            )
            putInt(
                ContentResolver.QUERY_ARG_SORT_DIRECTION,
                ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
            )
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
                images.add(MediaItem(mediaId = id, contentUri = contentUri))
                LogManager.d(message = "Camera Image: ID=$id, Path=$contentUri")
            }
        }
        return images
    }
}

