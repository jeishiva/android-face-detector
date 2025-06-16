package com.experiment.facedetector.data.local.worker

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
import com.experiment.facedetector.data.local.entities.MediaEntity
import com.experiment.facedetector.domain.entities.MediaItem
import com.experiment.facedetector.domain.entities.FaceDetectedMediaItem
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import com.experiment.facedetector.config.ThumbnailConfig
import com.experiment.facedetector.config.ThumbnailConfig.THUMBNAIL_SIZE
import com.experiment.facedetector.data.local.worker.entities.BatchResult
import com.experiment.facedetector.data.local.worker.entities.ProcessedImageResult
import com.experiment.facedetector.domain.repo.IMediaRepo

/**
 * Processes camera images concurrently with configurable parallelism using Semaphore
 * Queries images in batches, detects faces, creates thumbnails, and saves to database
 */
class CameraImageProcessor(
    private val context: Context,
    private val faceDetectionProcessor: FaceDetectionProcessor,
    private val mediaRepo: IMediaRepo,
    private val imageHelper: BitmapHelper,
    private val pageSize: Int = 20,
    private val concurrentLimit: Int = 3
) {
    private val processingLimit = Semaphore(concurrentLimit)

    suspend fun processAllImages() = withContext(Dispatchers.IO) {
        LogManager.d("CameraImageProcessor", "Starting concurrent image processing with limit: $concurrentLimit")
        var page = 0
        var totalProcessed = 0
        var totalSaved = 0
        while (true) {
            val images = queryCameraImages(pageSize, page * pageSize)
            if (images.isEmpty()) {
                LogManager.d(
                    "CameraImageProcessor",
                    "Completed processing. Total processed: $totalProcessed, Total saved: $totalSaved"
                )
                break
            }

            val pageResult = processImageBatchConcurrently(images)
            totalProcessed += pageResult.processed
            totalSaved += pageResult.saved
            LogManager.d(
                "CameraImageProcessor",
                "Page $page: ${pageResult.processed} processed, ${pageResult.saved} saved"
            )
            page++
        }
    }

    /**
     * process image batch with concurrent processing
     */
    private suspend fun processImageBatchConcurrently(images: List<MediaItem>): BatchResult {
        // Filter out already processed images
        val imagesToProcess = filterNewImages(images)
        if (imagesToProcess.isEmpty()) {
            return BatchResult(0, 0)
        }

        LogManager.d("CameraImageProcessor", "Processing ${imagesToProcess.size} new images concurrently")

        // Process images concurrently
        val processedResults = processImagesWithConcurrency(imagesToProcess)

        // Save processed images
        val savedEntities = saveProcessedImages(processedResults)

        // Batch insert all entities
        if (savedEntities.isNotEmpty()) {
            mediaRepo.insertOrUpdateMedia(savedEntities)
        }

        return BatchResult(processedResults.size, savedEntities.size)
    }

    /**
     * process multiple images concurrently with semaphore limiting
     */
    private suspend fun processImagesWithConcurrency(
        images: List<MediaItem>
    ): List<ProcessedImageResult> = withContext(Dispatchers.Default) {

        images.map { image ->
            async {
                // Acquire semaphore to limit concurrency
                processingLimit.acquire()
                try {
                    processImageSafely(image)
                } finally {
                    processingLimit.release()
                }
            }
        }.awaitAll()
            .filterNotNull() // Remove failed results
    }

    /**
     * process single image with comprehensive error handling
     */
    private suspend fun processImageSafely(image: MediaItem): ProcessedImageResult? {
        return try {
            val faceDetectedMediaItem = faceDetectionProcessor.processImage(image)

            if (faceDetectedMediaItem.faces.isEmpty()) {
                BitmapPool.put(faceDetectedMediaItem.image)
                LogManager.d("CameraImageProcessor", "No faces detected in image ${image.mediaId}")
                return null
            }

            // Create processed result without saving (decoupled)
            createProcessedImageResult(faceDetectedMediaItem)

        } catch (e: Exception) {
            LogManager.e("CameraImageProcessor", "Failed to process image ${image.mediaId}: ${e.message}", e)
            null
        }
    }

    /**
     * create processed image result without saving (decoupled processing)
     */
    private suspend fun createProcessedImageResult(
        faceImage: FaceDetectedMediaItem
    ): ProcessedImageResult? {
        var boundingBoxBitmap: Bitmap? = null
        var thumbnailBitmap: Bitmap? = null

        return try {
            // Draw bounding boxes
            boundingBoxBitmap = imageHelper.drawFaceBoundingBoxes(
                faceImage.image,
                faceImage.faces
            )

            // Create thumbnail
            thumbnailBitmap = imageHelper.scaleFromPool(
                boundingBoxBitmap,
                THUMBNAIL_SIZE, THUMBNAIL_SIZE
            )

            // Return result without saving
            ProcessedImageResult(
                mediaItem = faceImage.mediaItem,
                thumbnailBitmap = thumbnailBitmap,
                faces = faceImage.faces,
                originalBitmap = faceImage.image
            )

        } catch (e: Exception) {
            LogManager.e("CameraImageProcessor", "Failed to create processed image result for ${faceImage.mediaItem.mediaId}", e)
            // Clean up on failure
            cleanupBitmaps(faceImage.image, boundingBoxBitmap, thumbnailBitmap)
            null
        }
    }

    /**
     * save processed images to storage (I/O operations)
     */
    private suspend fun saveProcessedImages(
        processedResults: List<ProcessedImageResult>
    ): List<MediaEntity> = withContext(Dispatchers.IO) {

        processedResults.mapNotNull { result ->
            try {
                saveThumbnailSafely(result)
            } catch (e: Exception) {
                LogManager.e("CameraImageProcessor", "Failed to save image ${result.mediaItem.mediaId}: ${e.message}", e)
                null
            } finally {
                // Always cleanup bitmaps after saving attempt
                cleanupBitmaps(result.originalBitmap, result.thumbnailBitmap)
            }
        }
    }

    /**
     * save single thumbnail with error handling
     */
    private fun saveThumbnailSafely(result: ProcessedImageResult): MediaEntity? {
        return try {
            LogManager.d("CameraImageProcessor", "Saving thumbnail for image ${result.mediaItem.mediaId}")

            val file = imageHelper.saveBitmap(
                result.thumbnailBitmap,
                result.mediaItem.mediaId.toFileName(),
                ThumbnailConfig.THUMBNAIL_FORMAT,
                ThumbnailConfig.THUMBNAIL_QUALITY
            )

            if (file != null) {
                val media = MediaEntity(
                    mediaId = result.mediaItem.mediaId,
                    contentUri = result.mediaItem.contentUri.toString(),
                    thumbnailUri = file.absolutePath
                )
                LogManager.d("CameraImageProcessor", "Successfully saved: $media")
                media
            } else {
                LogManager.w("CameraImageProcessor", "Failed to save bitmap for ${result.mediaItem.mediaId}")
                null
            }
        } catch (e: Exception) {
            LogManager.e("CameraImageProcessor", "Exception saving thumbnail for ${result.mediaItem.mediaId}", e)
            null
        }
    }

    /**
     * filter new images using Set for O(1) lookup
     */
    private suspend fun filterNewImages(images: List<MediaItem>): List<MediaItem> {
        if (images.isEmpty()) return emptyList()

        val allIds = images.map { it.mediaId }
        // converting to set for O(1) lookup
        val existingIds = mediaRepo.getExistingMediaIds(allIds).toSet()
        return images.filterNot { it.mediaId in existingIds }
    }

    /**
     * clean up bitmaps by returning them to pool
     */
    private fun cleanupBitmaps(vararg bitmaps: Bitmap?) {
        bitmaps.filterNotNull().forEach { bitmap ->
            BitmapPool.put(bitmap)
        }
    }

    /**
     * query camera images with pagination
     */
    private fun queryCameraImages(limit: Int, offset: Int): List<MediaItem> {
        val images = mutableListOf<MediaItem>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.RELATIVE_PATH
        )
        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        return try {
            val cursor = context.contentResolver.query(
                queryUri,
                projection,
                getQueryArgs(limit, offset),
                null
            )

            cursor?.use {
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(queryUri, id)
                    images.add(MediaItem(mediaId = id, contentUri = contentUri))
                    LogManager.d("CameraImageProcessor", "Camera Image: ID=$id, Path=$contentUri")
                }
            }
            images
        } catch (e: Exception) {
            LogManager.e("CameraImageProcessor", "Failed to query camera images", e)
            emptyList()
        }
    }

    /**
     * create query arguments for MediaStore query
     */
    private fun getQueryArgs(limit: Int, offset: Int): Bundle {
        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("%DCIM/Camera%")

        return Bundle().apply {
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
    }
}

