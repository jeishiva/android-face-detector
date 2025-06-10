package com.experiment.facedetector.ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

@Composable
fun AppCircularProgressIndicator() {
    CircularProgressIndicator(
        modifier = Modifier
            .size(56.dp)
            .padding(8.dp),
        color = Color.White,
        strokeWidth = 4.dp
    )
}

/**
 * Reusable component for displaying full-screen images with face detection support.
 *
 * ⚠️ Do not change [ContentScale] — it is set intentionally to ensure
 * correct coordinate mapping between the original image and face bounding boxes.
 *
 * Use this component wherever full image rendering with face overlays is required.
 */

@Composable
fun AppFullScreenImage(imageBitmap: ImageBitmap, description: String? = null) {
    return Image(
        bitmap = imageBitmap,
        contentDescription = description,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.FillBounds
    )
}