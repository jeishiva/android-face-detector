package com.experiment.facedetector.data.local.source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.data.local.dao.MediaDao
import com.experiment.facedetector.domain.entities.UIImage
import java.io.File

class LocalCameraPagingSource(
    private val mediaDao: MediaDao,
) : PagingSource<Int, UIImage>() {

    override fun getRefreshKey(state: PagingState<Int, UIImage>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPosition)
        return page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UIImage> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize
            val offset = page * pageSize
            val mediaEntities = mediaDao.getPagedMedia(pageSize, offset)
            val faceImages = mediaEntities.map { entity ->
                UIImage(
                    mediaId = entity.mediaId,
                    file = File(entity.thumbnailUri)
                ) .also {
                    LogManager.d("LocalCameraPagingSource", "Loaded image with ID $it")
                }
            }
            LoadResult.Page(
                data = faceImages,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (mediaEntities.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LogManager.e(message = "Paging load error: ${e.localizedMessage}")
            LoadResult.Error(e)
        }
    }
}
