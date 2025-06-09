package com.experiment.facedetector.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.experiment.facedetector.ui.theme.AndroidFaceDetectorTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.experiment.facedetector.domain.entities.FaceImage
import com.experiment.facedetector.viewmodel.FullImageViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun FullImageScreen(faceImage: FaceImage) {
    val viewModel: FullImageViewModel = koinViewModel()
    AndroidFaceDetectorTheme {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Hello, World ${faceImage.userImage.mediaId}!",
                    color = Color.Black,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
        }
    }
}