package com.experiment.facedetector.ui.screen

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.experiment.facedetector.R
import com.experiment.facedetector.navigation.AppRoute
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.config.AppConfig
import com.experiment.facedetector.domain.entities.ProcessedMediaItem
import com.experiment.facedetector.ui.entities.MildGrayPainter
import com.experiment.facedetector.ui.theme.AndroidFaceDetectorTheme
import com.experiment.facedetector.ui.theme.MildGray
import com.experiment.facedetector.ui.widgets.AppBar
import com.experiment.facedetector.ui.widgets.AppCircularProgressIndicator
import com.experiment.facedetector.viewmodel.GalleryViewModel
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(navController: NavHostController) {
    val viewModel: GalleryViewModel = koinViewModel()
    val imageLoader: ImageLoader = koinInject()
    val isWorkerRunning by viewModel.isWorkerRunning.collectAsState()
    val activity = LocalContext.current as? Activity

    LogManager.d(message = "rendering gallery screen")

    InitializeWork(viewModel)

    AndroidFaceDetectorTheme {
        Scaffold(
            topBar = { GalleryTopBar(activity) },
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            GalleryContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                viewModel = viewModel,
                imageLoader = imageLoader,
                isLoadingPhotos = isWorkerRunning,
                navController = navController
            )
        }
    }
}

@Composable
private fun InitializeWork(viewModel: GalleryViewModel) {
    var workedStarted by rememberSaveable { mutableStateOf(false) }
    if (workedStarted.not()) {
        workedStarted = true
        viewModel.startInitialWork()
        LogManager.d(message = "start work")
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun GalleryTopBar(activity: Activity?) {
    AppBar(
        title = stringResource(R.string.gallery),
    ) {
        activity?.finish()
    }
}

@Composable
private fun GalleryContent(
    modifier: Modifier = Modifier,
    viewModel: GalleryViewModel,
    imageLoader: ImageLoader,
    isLoadingPhotos: Boolean,
    navController: NavHostController
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CameraImageGrid(
            imagesFlow = viewModel.userImageFlow,
            columns = getColumnCount(),
            spacing = 8.dp,
            imageLoader = imageLoader,
            onItemClick = { mediaId ->
                navController.navigate(AppRoute.FullImage.createRoute(mediaId))
            },
            isLoadingPhotos = isLoadingPhotos
        )
    }
}

@Composable
private fun getColumnCount(): Int {
    return if (isLandscape()) {
        AppConfig.GRID_SIZE * 2
    } else {
        AppConfig.GRID_SIZE
    }
}

@Composable
private fun isLandscape(): Boolean {
    val configuration = LocalContext.current.resources.configuration
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

@Composable
fun CameraImageGrid(
    imagesFlow: Flow<PagingData<ProcessedMediaItem>>,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier,
    columns: Int,
    spacing: Dp,
    isLoadingPhotos: Boolean,
    onItemClick: (Long) -> Unit
) {
    val lazyPagingItems = imagesFlow.collectAsLazyPagingItems()
    val gridState = rememberLazyGridState()
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        GridLoadStateHandler(
            lazyPagingItems = lazyPagingItems,
            imageLoader = imageLoader,
            columns = columns,
            spacing = spacing,
            onItemClick = onItemClick,
            isLoadingPhotos = isLoadingPhotos,
            gridState = gridState
        )
    }
}

@Composable
private fun GridLoadStateHandler(
    lazyPagingItems: LazyPagingItems<ProcessedMediaItem>,
    imageLoader: ImageLoader,
    columns: Int,
    spacing: Dp,
    isLoadingPhotos: Boolean,
    onItemClick: (Long) -> Unit,
    gridState: LazyGridState
) {
    if (lazyPagingItems.itemCount > 0) {
        ImageGrid(
            lazyPagingItems = lazyPagingItems,
            imageLoader = imageLoader,
            columns = columns,
            spacing = spacing,
            appendState = lazyPagingItems.loadState.append,
            onItemClick = onItemClick,
            isLoadingPhotos = isLoadingPhotos,
            gridState = gridState
        )
    } else {
        when (lazyPagingItems.loadState.refresh) {
            is LoadState.Loading -> AppCircularProgressIndicator()
            is LoadState.Error -> ErrorMessage(error = (lazyPagingItems.loadState.refresh as LoadState.Error).error)
            else -> {}
        }
    }
}

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
private fun ImageGrid(
    lazyPagingItems: LazyPagingItems<ProcessedMediaItem>,
    imageLoader: ImageLoader,
    columns: Int,
    spacing: Dp,
    appendState: LoadState,
    isLoadingPhotos: Boolean,
    onItemClick: (Long) -> Unit,
    gridState: LazyGridState
) {
    val imageSize = calculateOptimalImageSize(columns, spacing)
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        gridItems(
            lazyPagingItems = lazyPagingItems,
            imageSize = imageSize,
            imageLoader = imageLoader,
            onItemClick = onItemClick,
        )
        gridAppendState(appendState, isLoadingPhotos)
    }
}

private fun LazyGridScope.gridItems(
    lazyPagingItems: LazyPagingItems<ProcessedMediaItem>,
    imageSize: Dp,
    imageLoader: ImageLoader,
    onItemClick: (Long) -> Unit
) {
    items(
        count = lazyPagingItems.itemCount,
        key = { index -> lazyPagingItems[index]?.mediaId ?: "placeholder-$index" }
    ) { index ->
        val image = lazyPagingItems[index]
        if (image != null) {
            UserImageItem(
                image = image,
                size = imageSize,
                imageLoader = imageLoader,
                onClick = onItemClick
            )
        }
    }
}

private fun LazyGridScope.gridAppendState(
    appendState: LoadState,
    isLoadingPhotos: Boolean,
) {
    when {
        isLoadingPhotos || appendState is LoadState.Loading -> {
            item {
                GridItemLoader()
                LogManager.d("CameraImageGrid", "Append state: Loading from WorkManager")
            }
        }
        appendState is LoadState.Error -> {
            item {
                AppendErrorMessage(appendState.error)
                LogManager.e("CameraImageGrid", "Append error", appendState.error)
            }
        }
    }
}

@Composable
private fun AppendErrorMessage(error: Throwable) {
    Text(
        text = "Error loading more: ${error.message}",
        color = Color.Red,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )
}

@Composable
private fun calculateOptimalImageSize(columns: Int, spacing: Dp): Dp {
    val context = LocalContext.current
    val densityValue = LocalDensity.current.density
    val screenWidthPx = context.resources.displayMetrics.widthPixels

    return remember(screenWidthPx, columns, spacing, densityValue) {
        calculateImageSize(
            screenWidthPx = screenWidthPx,
            density = densityValue,
            columns = columns,
            spacing = spacing
        )
    }
}

private fun calculateImageSize(
    screenWidthPx: Int,
    density: Float,
    columns: Int,
    spacing: Dp
): Dp {
    val spacingPx = spacing.value * density
    val totalSpacingPx = spacingPx * (columns - 1)
    val availableWidthPx = screenWidthPx - totalSpacingPx
    val imageSizePx = availableWidthPx / columns
    return Dp(imageSizePx / density)
}

@Composable
private fun GridItemLoader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .wrapContentSize(Alignment.Center)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 4.dp,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun UserImageItem(
    image: ProcessedMediaItem,
    size: Dp,
    imageLoader: ImageLoader,
    onClick: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val request = remember(image.file.path) {
        ImageRequest.Builder(context)
            .data(image.file)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.DISABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .error(android.R.drawable.stat_notify_error)
            .listener(
                onSuccess = { _, _ ->
                    LogManager.d("UserImageItem", "Loaded image ${image.mediaId}")
                },
                onError = { _, result ->
                    LogManager.e(
                        "UserImageItem",
                        "Failed to load image ${image.mediaId}",
                        result.throwable
                    )
                }
            )
            .build()
    }
    Image(
        painter = rememberAsyncImagePainter(
            model = request,
            imageLoader = imageLoader,
            placeholder = MildGrayPainter
        ),
        contentDescription = "Image with ID ${image.mediaId} from camera",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick(image.mediaId) }
    )
}


@Composable
private fun rememberImageRequest(image: ProcessedMediaItem, context: Context): ImageRequest {
    return remember(image) {
        ImageRequest.Builder(context)
            .data(image.file)
            .diskCachePolicy(CachePolicy.DISABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
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
            .build()
    }
}
