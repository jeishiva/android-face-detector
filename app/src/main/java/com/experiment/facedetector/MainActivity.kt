package com.experiment.facedetector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.experiment.facedetector.ui.screen.GalleryScreen
import com.experiment.facedetector.ui.screen.SplashScreen
import com.experiment.facedetector.ui.theme.AndroidFaceDetectorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppEntryPoint()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppEntryPoint() {
    AndroidFaceDetectorTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            MainApp()
        }
    }
}

@Composable
fun MainApp() {
    var splashCompleted by remember { mutableStateOf(false) }
    if (!splashCompleted) {
        SplashScreen {
            splashCompleted = true
        }
    } else {
        GalleryScreen()
    }
}



