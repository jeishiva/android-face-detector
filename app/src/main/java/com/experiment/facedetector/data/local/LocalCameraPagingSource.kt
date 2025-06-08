package com.experiment.facedetector.data.local

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.core.FaceDetectionProcessor
import com.experiment.facedetector.domain.entities.FaceImage
import com.experiment.facedetector.domain.entities.UserImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalCameraPagingSource(
    private val context: Context,
    private val pageSize: Int,
    private val faceDetectionProcessor: FaceDetectionProcessor,
) : PagingSource<Int, FaceImage>() {

    override fun getRefreshKey(state: PagingState<Int, FaceImage>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val closestPage = state.closestPageToPosition(anchorPosition) ?: return null
        return closestPage.prevKey?.plus(1) ?: closestPage.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FaceImage> {
        return withContext(Dispatchers.IO) {
            try {
                val page = params.key ?: 0
                val offset = page * pageSize
                val rawImages = queryCameraImages(limit = pageSize, offset = offset)
                val resultImages = mutableListOf<FaceImage>()

                for (image in rawImages) {
                    try {
                        val processed = faceDetectionProcessor.processImage(image)
                        resultImages.add(processed)
                    } catch (ignored: Exception) {
                        ignored.printStackTrace()
                        LogManager.e(message = "image skipped ${image.contentUri}")
                    }
                }

                LoadResult.Page(
                    data = resultImages,
                    prevKey = if (page == 0) null else page - 1,
                    nextKey = if (rawImages.isEmpty()) null else page + 1
                )
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
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
