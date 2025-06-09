package com.experiment.facedetector.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.experiment.facedetector.data.local.entities.MediaEntity

@Dao
interface MediaDao {
    @Query("SELECT * FROM media ORDER BY mediaId DESC")
    fun getAllMedia(): PagingSource<Int, MediaEntity>

    @Query("SELECT * FROM media ORDER BY mediaId DESC LIMIT :limit OFFSET :offset")
    suspend fun getPagedMedia(limit: Int, offset: Int): List<MediaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: MediaEntity)

    @Delete
    suspend fun deleteMedia(media: MediaEntity)

    @Query("SELECT mediaId FROM media WHERE mediaId IN (:mediaIds)")
    suspend fun getExistingMediaIds(mediaIds: List<Long>): List<Long>

}
