package com.experiment.facedetector.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media")
data class MediaEntity(
    @PrimaryKey val mediaId: Long,
    val contentUri: String,
    val thumbnailUri: String
)

