package com.experiment.facedetector.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.domain.entities.UserImage
import com.experiment.facedetector.ui.theme.AndroidFaceDetectorTheme
import com.experiment.facedetector.viewmodel.GalleryViewModel
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Preview(showBackground = true)
@Composable
fun GalleryScreen() {
    val viewModel: GalleryViewModel = koinViewModel()
    val imageLoader: ImageLoader = koinInject()
    LogManager.d(message = "rendering gallery screen")

    AndroidFaceDetectorTheme {
        Scaffold(
            containerColor = Color.Transparent, modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CameraImageGrid(imagesFlow = viewModel.userImageFlow, imageLoader)
            }
        }
    }
}

@Composable
fun CameraImageGrid(
    imagesFlow: Flow<PagingData<UserImage>>,
    imageLoader: ImageLoader
) {
    val lazyPagingItems: LazyPagingItems<UserImage> = imagesFlow.collectAsLazyPagingItems()
    val context = LocalContext.current
    val screenWidth = context.resources.displayMetrics.widthPixels
    val spacingPx = with(LocalDensity.current) { 8.dp.toPx() }
    val totalSpacing = spacingPx * 4
    val imageSizePx = ((screenWidth - totalSpacing) / 3).toInt()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.padding(8.dp)
    ) {
        items(lazyPagingItems.itemCount) { index ->
            val image = lazyPagingItems[index]
            if (image != null) {
                UserImageItem(image, imageSizePx.dp, imageLoader)
            }
        }

        lazyPagingItems.apply {
            when {
                loadState.refresh is LoadState.Loading -> {
                }

                loadState.append is LoadState.Loading -> {
                }

                loadState.append is LoadState.Error -> {
                }
            }
        }
    }
}

@Composable
fun UserImageItem(image: UserImage, size: Dp, imageLoader: ImageLoader) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(image.contentUri)
            .crossfade(true)
            .error(android.R.drawable.stat_notify_error)
            .build(),
        contentDescription = null,
        modifier = Modifier
            .size(150.dp)
            .clip(RoundedCornerShape(8.dp)),
        placeholder = ColorPainter(Color(0xFFEEEEEE)),
        contentScale = ContentScale.Crop,
        imageLoader = imageLoader
    )
}
