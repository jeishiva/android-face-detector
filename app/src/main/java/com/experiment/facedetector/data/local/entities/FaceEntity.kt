package com.experiment.facedetector.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "faces",
    foreignKeys = [
        ForeignKey(
            entity = MediaEntity::class,
            parentColumns = ["mediaId"],
            childColumns = ["mediaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("mediaId")]
)
data class FaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mediaId: Long,
    val faceId: String,
    val tag : String
)
