package com.experiment.facedetector.data.local

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.domain.entities.UserImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalCameraPagingSource(
    private val context: Context, private val pageSize: Int
) : PagingSource<Int, UserImage>() {

    override fun getRefreshKey(state: PagingState<Int, UserImage>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val closestPage = state.closestPageToPosition(anchorPosition) ?: return null
        return closestPage.prevKey?.plus(1) ?: closestPage.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UserImage> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize
            val allImages = withContext(Dispatchers.IO) {
                queryCameraImages()
            }
            val pagedImages = allImages.drop(page * pageSize).take(pageSize)
            LoadResult.Page(
                data = pagedImages,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (pagedImages.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private fun queryCameraImages(): List<UserImage> {
        val images = mutableListOf<UserImage>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.RELATIVE_PATH
        )
        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("%DCIM/Camera%")
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cursor = context.contentResolver.query(
            queryUri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
        cursor?.use {
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val contentUri = ContentUris.withAppendedId(queryUri, id)
                images.add(UserImage(id = id, contentUri = contentUri))
                LogManager.d (message = "Camera Image: ID=$id, Path=$contentUri")
            }
        }
        return images
    }

}
