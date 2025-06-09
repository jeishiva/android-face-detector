package com.experiment.facedetector.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.domain.entities.UIImage
import com.experiment.facedetector.ui.theme.AndroidFaceDetectorTheme
import com.experiment.facedetector.ui.theme.MildGray
import com.experiment.facedetector.viewmodel.GalleryViewModel
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(navController: NavHostController) {
    val viewModel: GalleryViewModel = koinViewModel()
    val imageLoader: ImageLoader = koinInject()
    LogManager.d(message = "rendering gallery screen")
    var workedStarted by rememberSaveable { mutableStateOf(false) }
    if (workedStarted.not()) {
        workedStarted = true
        viewModel.startInitialWork()
        LogManager.d(message = "start work")
    }
    AndroidFaceDetectorTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Gallery") },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            containerColor = Color.Transparent, modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CameraImageGrid(
                    imagesFlow = viewModel.userImageFlow,
                    columns = getColumnCount(),
                    spacing = 8.dp,
                    imageLoader = imageLoader
                )
            }
        }
    }
}

@Composable
fun getColumnCount(): Int {
    return if (isLandscape()) {
        4
    } else {
        2
    }
}

@Composable
fun CameraImageGrid(
    imagesFlow: Flow<PagingData<UIImage>>,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier,
    columns: Int,
    spacing: Dp,
) {
    val lazyPagingItems = imagesFlow.collectAsLazyPagingItems()
    LogManager.d("CameraImageGrid", "Item count: ${lazyPagingItems.itemCount}")
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LoadStateContent(
            refreshState = lazyPagingItems.loadState.refresh,
            appendState = lazyPagingItems.loadState.append,
            lazyPagingItems = lazyPagingItems,
            imageLoader = imageLoader,
            columns = columns,
            spacing = spacing
        )
    }
}

@Composable
private fun LoadStateContent(
    refreshState: LoadState,
    appendState: LoadState,
    lazyPagingItems: LazyPagingItems<UIImage>,
    imageLoader: ImageLoader,
    columns: Int,
    spacing: Dp
) {
    when {
        refreshState is LoadState.Loading && lazyPagingItems.itemCount == 0 -> {
            // Show loading only when no items are loaded initially
            LoadingIndicator()
            LogManager.d("CameraImageGrid", "Refresh state: Loading, no items")
        }

        refreshState is LoadState.Error -> {
            ErrorMessage(error = refreshState.error)
            LogManager.e("CameraImageGrid", "Refresh error", refreshState.error)
        }

        else -> {
            ImageGridContent(
                lazyPagingItems = lazyPagingItems,
                imageLoader = imageLoader,
                columns = columns,
                spacing = spacing,
                appendState = appendState
            )
        }
    }
}

// Displays a loading indicator
@Composable
private fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    CircularProgressIndicator(
        modifier = modifier
            .size(72.dp)
            .padding(16.dp)
    )
}

// Displays an error message
@Composable
private fun ErrorMessage(
    error: Throwable,
    modifier: Modifier = Modifier
) {
    Text(
        text = "Error loading images: ${error.message}",
        color = Color.Red,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    )
}

@Composable
private fun ImageGridContent(
    lazyPagingItems: LazyPagingItems<UIImage>,
    imageLoader: ImageLoader,
    columns: Int,
    spacing: Dp,
    appendState: LoadState
) {
    val imageSize = calculateImageSize(columns, spacing)
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        items(
            count = lazyPagingItems.itemCount,
            key = { index -> lazyPagingItems[index]?.mediaId ?: "placeholder-$index" } // Stable key
        ) { index ->
            val image = lazyPagingItems[index]
            if (image != null) {
                UserImageItem(image, imageSize, imageLoader)
            }
        }
        appendStateContent(appendState)
    }
}

@Composable
fun isLandscape(): Boolean {
    val configuration = LocalContext.current.resources.configuration
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

@Composable
private fun calculateImageSize(columns: Int, spacing: Dp): Dp {
    val context = LocalContext.current
    val screenWidthPx = context.resources.displayMetrics.widthPixels
    val density = LocalDensity.current
    val spacingPx = with(density) { spacing.toPx() }
    val totalSpacingPx = spacingPx * (columns - 1)

    val availableWidthPx = screenWidthPx - totalSpacingPx
    val imageSizePx = availableWidthPx / columns

    return with(density) { imageSizePx.toDp() }
}

private fun LazyGridScope.appendStateContent(appendState: LoadState) {
    when (appendState) {
        is LoadState.Loading -> {
            item {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .size(72.dp)
                        .padding(16.dp)
                )
                LogManager.d("CameraImageGrid", "Append state: Loading")
            }
        }

        is LoadState.Error -> {
            item {
                Text(
                    text = "Error loading more: ${appendState.error.message}",
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
                LogManager.e("CameraImageGrid", "Append error", appendState.error)
            }
        }

        else -> {

        }
    }
}

@Composable
fun UserImageItem(
    image: UIImage,
    size: Dp,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(image.file)
            .crossfade(true)
            .error(android.R.drawable.stat_notify_error)
            .listener(
                onError = { _, result ->
                    LogManager.e(
                        "UserImageItem",
                        "Failed to load image ${image.mediaId}",
                        result.throwable
                    )
                },
                onSuccess = { _, _ ->
                    LogManager.d("UserImageItem", "Loaded image ${image.mediaId}")
                }
            )
            .build(),
        contentDescription = "Image with ID ${image.mediaId} from camera",
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp)),
        placeholder = ColorPainter(MildGray),
        contentScale = ContentScale.Crop,
        imageLoader = imageLoader
    )
}