package com.experiment.facedetector.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.experiment.facedetector.viewmodel.FullImageViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun FullImageScreen(mediaId: Long) {
    val viewModel: FullImageViewModel = koinViewModel()
    val mediaState = viewModel.media.collectAsState()

    Scaffold(
        containerColor = Color.Transparent, modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (val mediaEntity = mediaState.value) {
                null -> CircularProgressIndicator(
                    modifier = Modifier
                        .size(72.dp)
                        .padding(16.dp)
                )

                else -> {
                    Text("Add Image ${mediaEntity.mediaId}")
                }
            }
        }
    }
}