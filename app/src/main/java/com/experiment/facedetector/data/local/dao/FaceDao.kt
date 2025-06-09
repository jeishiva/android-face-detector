package com.experiment.facedetector.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.experiment.facedetector.data.local.entities.FaceEntity

@Dao
interface FaceDao {
    @Query("SELECT * FROM faces WHERE mediaId = :mediaId")
    suspend fun getFacesForMedia(mediaId: String): List<FaceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaces(faces: List<FaceEntity>)

    @Query("DELETE FROM faces WHERE mediaId = :mediaId")
    suspend fun deleteFaces(mediaId: String)
}
