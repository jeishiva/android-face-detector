package com.experiment.facedetector.ui.screen

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.domain.entities.FaceTag
import com.experiment.facedetector.image.ImageCoordinateHelper
import com.experiment.facedetector.ui.widgets.AppCircularProgressIndicator
import com.experiment.facedetector.viewmodel.FullImageViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullImageScreen() {
    val viewModel: FullImageViewModel = koinViewModel()
    val fullImageResult = viewModel.fullImageResult.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Full Image") }, navigationIcon = {
                IconButton(onClick = {
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            })
        },
        containerColor = Color.Transparent, modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (val result = fullImageResult.value) {
                null -> AppCircularProgressIndicator()
                else -> {
                    FullImageWithFaceOverlay(
                        bitmap = result.thumbnail,
                        faceTags = result.faces,
                        onFaceClick = { faceTag ->
                            LogManager.d("FaceClick", "Clicked face bounds ${faceTag.id}:")
                        })
                }
            }
        }
    }
}

@Composable
fun FullImageWithFaceOverlay(
    bitmap: Bitmap,
    faceTags: List<FaceTag>,
    onFaceClick: (FaceTag) -> Unit
) {
    val imageBitmap = bitmap.asImageBitmap()
    val density = LocalDensity.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

        val containerWidthPx = with(density) { this@BoxWithConstraints.maxWidth.toPx() }
        val containerHeightPx = with(density) { this@BoxWithConstraints.maxHeight.toPx() }

        val coordinateHelper = remember(containerWidthPx, containerHeightPx, bitmap.width, bitmap.height) {
             ImageCoordinateHelper(
                containerWidthPx,
                containerHeightPx,
                bitmap.width.toFloat(),
                bitmap.height.toFloat()
            )
        }

        Image(
            bitmap = imageBitmap,
            contentDescription = "Full Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit // FitXY equivalent with coordinate consistency
        )

        faceTags.forEach { face ->
            val faceLeft = face.left.toFloat()
            val faceTop = face.top.toFloat()
            val faceWidth = face.width.toFloat()
            val faceHeight = face.height.toFloat()

            val (left, top) = coordinateHelper.imageToContainerCoords(faceLeft, faceTop)
            val width = coordinateHelper.scaleWidth(faceWidth)
            val height = coordinateHelper.scaleHeight(faceHeight)
            Box(
                modifier = Modifier
                    .absoluteOffset(
                        x = with(density) { left.toDp() },
                        y = with(density) { top.toDp() }
                    )
                    .size(
                        width = with(density) { width.toDp() },
                        height = with(density) { height.toDp() }
                    )
                    .border(2.dp, Color.Green)
                    .clickable { onFaceClick(face) }
            )
        }
    }
}







