package com.experiment.facedetector.repo

import com.experiment.facedetector.data.local.dao.FaceDao
import com.experiment.facedetector.data.local.dao.MediaDao
import com.experiment.facedetector.data.local.entities.FaceEntity
import com.experiment.facedetector.data.local.entities.MediaEntity

class MediaRepo(val mediaDao: MediaDao, val faceDao: FaceDao) {

    suspend fun getMedia(mediaId: Long): MediaEntity {
        return mediaDao.getMediaEntityById(mediaId)
    }

    suspend fun getFaces(mediaId: Long): List<FaceEntity> {
        return faceDao.getFacesForMedia(mediaId)
    }

}