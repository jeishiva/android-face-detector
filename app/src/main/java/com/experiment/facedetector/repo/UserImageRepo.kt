package com.experiment.facedetector.repo

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.experiment.facedetector.data.local.dao.MediaDao
import com.experiment.facedetector.data.local.source.LocalCameraPagingSource
import com.experiment.facedetector.domain.entities.FaceMediaGridItem
import kotlinx.coroutines.flow.Flow

class UserImageRepo(
    private val context: Context,
    private val mediaDao: MediaDao,
) {
    private var currentPagingSource: LocalCameraPagingSource? = null

    fun getUserImageStream(pageSize: Int): Flow<PagingData<FaceMediaGridItem>> {
        return Pager(
            config = PagingConfig(pageSize = pageSize), pagingSourceFactory = {
                currentPagingSource = LocalCameraPagingSource(mediaDao)
                currentPagingSource!!
            }).flow
    }

    fun invalidatePagingSource() {
        currentPagingSource?.invalidate()
    }
}
