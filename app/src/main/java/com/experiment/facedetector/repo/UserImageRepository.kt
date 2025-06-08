package com.experiment.facedetector.repo

import android.content.Context
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.experiment.facedetector.core.FaceDetectionProcessor
import com.experiment.facedetector.data.local.LocalCameraPagingSource
import com.experiment.facedetector.domain.entities.FaceImage
import kotlinx.coroutines.flow.Flow

class UserImageRepository(
    private val context: Context, private val faceDetectionProcessor: FaceDetectionProcessor
) {
    fun getUserImageStream(pageSize: Int): Flow<PagingData<FaceImage>> {
        return Pager(
            config = PagingConfig(pageSize = pageSize), pagingSourceFactory = {
                LocalCameraPagingSource(
                    context,
                    pageSize,
                    faceDetectionProcessor
                )
            }).flow
    }
}
