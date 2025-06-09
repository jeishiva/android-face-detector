package com.experiment.facedetector.repo

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.experiment.facedetector.data.local.dao.MediaDao
import com.experiment.facedetector.data.local.source.LocalCameraPagingSource
import com.experiment.facedetector.domain.entities.UIImage
import kotlinx.coroutines.flow.Flow

class UserImageRepository(
    private val context: Context,
    private val mediaDao: MediaDao,
) {
    fun getUserImageStream(pageSize: Int): Flow<PagingData<UIImage>> {
        return Pager(
            config = PagingConfig(pageSize = pageSize), pagingSourceFactory = {
                LocalCameraPagingSource(mediaDao)
            }).flow
    }
}
