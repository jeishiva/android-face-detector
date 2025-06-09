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

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertMedia(media: MediaEntity)

    @Delete
    suspend fun deleteMedia(media: MediaEntity)
}