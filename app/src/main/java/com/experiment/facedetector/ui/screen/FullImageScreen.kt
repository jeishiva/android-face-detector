package com.experiment.facedetector.ui.screen

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.experiment.facedetector.domain.entities.FaceTag
import com.experiment.facedetector.image.ImageCoordinateHelper
import com.experiment.facedetector.ui.widgets.AppCircularProgressIndicator
import com.experiment.facedetector.viewmodel.FullImageViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullImageScreen(navController: NavHostController) {
    val viewModel: FullImageViewModel = koinViewModel()
    val fullImageResult = viewModel.fullImageResult.collectAsState()
    var editingFaceId by remember { mutableStateOf<String?>(null) }
    var currentInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Full Image") }, navigationIcon = {
                IconButton(onClick = {
                    navController.popBackStack()
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
                        bitmap = result.image,
                        faceTags = result.faces,
                        editingFaceId = editingFaceId,
                        onFaceClick = { face ->
                            editingFaceId = face.id
                            currentInput = face.tag
                        },
                        onTagChanged = { face, newTag ->
                            editingFaceId = null
                            currentInput = ""
                            viewModel.saveFaceTag(result.mediaId, face, newTag)
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
    editingFaceId: String?,
    onFaceClick: (FaceTag) -> Unit,
    onTagChanged: (FaceTag, String) -> Unit
) {
    val imageBitmap = bitmap.asImageBitmap()
    val density = LocalDensity.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

        val containerWidthPx = with(density) { this@BoxWithConstraints.maxWidth.toPx() }
        val containerHeightPx = with(density) { this@BoxWithConstraints.maxHeight.toPx() }

        val coordinateHelper =
            remember(containerWidthPx, containerHeightPx, bitmap.width, bitmap.height) {
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
            contentScale = ContentScale.Fit
        )

        faceTags.forEach { face ->
            key(face.id) {
                val faceLeft = face.left.toFloat()
                val faceTop = face.top.toFloat()
                val faceWidth = face.width.toFloat()
                val faceHeight = face.height.toFloat()

                val (left, top) = coordinateHelper.imageToContainerCoords(faceLeft, faceTop)
                val width = coordinateHelper.scaleWidth(faceWidth)
                val height = coordinateHelper.scaleHeight(faceHeight)

                Column(
                    modifier = Modifier.absoluteOffset(
                        x = with(density) { left.toDp() },
                        y = with(density) { top.toDp() }),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(
                                width = with(density) { width.toDp() },
                                height = with(density) { height.toDp() })
                            .border(2.dp, Color.Green)
                            .clickable {
                                onFaceClick(face)
                            }) {
                        if (editingFaceId == face.id) {
                            var tagText by remember { mutableStateOf(face.tag) }
                            TextField(
                                value = tagText,
                                onValueChange = { tagText = it },
                                singleLine = true,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(2.dp),
                                textStyle = MaterialTheme.typography.labelLarge,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0x80000000),
                                    unfocusedContainerColor = Color(0x80000000),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    onTagChanged(face, tagText)
                                }),
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
                            )
                        }
                    }
                    if (face.tag.isNotEmpty()) {
                        Text(
                            text = face.tag,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier
                                .background(Color(0x80000000), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}







