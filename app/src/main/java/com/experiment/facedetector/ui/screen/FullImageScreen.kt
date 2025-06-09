package com.experiment.facedetector.ui.screen

import android.widget.ImageView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.experiment.facedetector.viewmodel.FullImageViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullImageScreen() {
    val viewModel: FullImageViewModel = koinViewModel()
    val mediaState = viewModel.media.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Full Image") }, navigationIcon = {
                IconButton(onClick = { }) {
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
            when (val media = mediaState.value) {
                null -> CircularProgressIndicator(
                    modifier = Modifier
                        .size(72.dp)
                        .padding(16.dp)
                )

                else -> {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            ImageView(context).apply {
                                scaleType = ImageView.ScaleType.FIT_CENTER
                                setImageBitmap(media)
                            }
                        },
                        update = { imageView ->
                            imageView.setImageBitmap(media)
                        }
                    )
                }
            }
        }
    }
}