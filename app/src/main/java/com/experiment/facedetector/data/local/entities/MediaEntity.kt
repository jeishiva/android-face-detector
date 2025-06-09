package com.experiment.facedetector.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media")
data class MediaEntity(
    @PrimaryKey val mediaId: String,
    val contentUri: String,
    val thumbnailUri: String
)