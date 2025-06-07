package com.experiment.facedetector.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
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
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier,
    columns: Int = 3,
    spacing: Dp = 8.dp
) {
    val lazyPagingItems = imagesFlow.collectAsLazyPagingItems()
    LogManager.d("CameraImageGrid", "Item count: ${lazyPagingItems.itemCount}")

    Box(modifier = modifier.fillMaxSize()) {
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

// Handles load state and displays content, loading, or error
@Composable
private fun LoadStateContent(
    refreshState: LoadState,
    appendState: LoadState,
    lazyPagingItems: LazyPagingItems<UserImage>,
    imageLoader: ImageLoader,
    columns: Int,
    spacing: Dp
) {
    when (refreshState) {
        is LoadState.Loading -> {
            LoadingIndicator()
            LogManager.d("CameraImageGrid", "Refresh state: Loading")
        }
        is LoadState.Error -> {
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
            .size(48.dp) // Fixed size for consistency
            .padding(16.dp) // Optional padding for spacing
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
        modifier = modifier.padding(16.dp)
    )
}

// Renders the grid of images
@Composable
private fun ImageGridContent(
    lazyPagingItems: LazyPagingItems<UserImage>,
    imageLoader: ImageLoader,
    columns: Int,
    spacing: Dp,
    appendState: LoadState
) {
    val imageSize = calculateImageSize(columns, spacing)
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing)
    ) {
        items(lazyPagingItems.itemCount) { index ->
            val image = lazyPagingItems[index]
            if (image != null) {
                UserImageItem(image, imageSize, imageLoader)
            }
        }
        appendStateContent(appendState)
    }
}

@Composable
private fun calculateImageSize(columns: Int, spacing: Dp): Dp {
    val context = LocalContext.current
    val screenWidthPx = context.resources.displayMetrics.widthPixels
    val spacingPx = with(LocalDensity.current) { spacing.toPx() }
    val totalSpacingPx = spacingPx * (columns + 1)
    return with(LocalDensity.current) { ((screenWidthPx - totalSpacingPx) / columns).toInt().toDp() }
}

private fun LazyGridScope.appendStateContent(appendState: LoadState) {
    when (appendState) {
        is LoadState.Loading -> {
            item {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
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

// Displays a single user image item
@Composable
fun UserImageItem(
    image: UserImage,
    size: Dp,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(image.contentUri)
            .crossfade(true)
            .error(android.R.drawable.stat_notify_error)
            .listener(
                onError = { _, result ->
                    LogManager.e("UserImageItem", "Failed to load image ${image.id}", result.throwable)
                },
                onSuccess = { _, _ ->
                    LogManager.d("UserImageItem", "Loaded image ${image.id}")
                }
            )
            .build(),
        contentDescription = "Image with ID ${image.id} from camera",
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp)),
        placeholder = ColorPainter(Color.Gray),
        contentScale = ContentScale.Crop,
        imageLoader = imageLoader
    )
}