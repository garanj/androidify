package com.android.developers.androidify.customize

import android.R.attr.bitmap
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toIntSize
import coil3.compose.AsyncImage
import com.android.developers.androidify.results.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.graphics.createBitmap

@Composable
fun CustomizedImageRenderer(
    exportImageCanvas: ExportImageCanvas,
    modifier: Modifier = Modifier,
) {
    ImageResult(
        exportImageCanvas,
        modifier,
    )
}

@Composable
private fun ImageResult(
    exportImageCanvas: ExportImageCanvas,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(exportImageCanvas.aspectRatio, matchHeightConstraintsFirst = true)
                .drawWithCache {
                    onDrawBehind {
                        val renderScale = this.size.width / exportImageCanvas.canvasSize.width
                        // scale up the drawing commands to the size of the incoming surface!
                        scale(renderScale, renderScale, pivot = Offset.Zero) {
                            if (exportImageCanvas.selectedBackground != null) {
                                drawImage(
                                    image = exportImageCanvas.selectedBackground!!.asImageBitmap())
                            }
                            if (exportImageCanvas.imageBitmap != null) {
                                drawImage(
                                    exportImageCanvas.imageBitmap.asImageBitmap(),
                                    srcSize = exportImageCanvas.canvasSize.toIntSize(),
                                    srcOffset = IntOffset(
                                        exportImageCanvas.imageRect.left.toInt(),
                                        exportImageCanvas.imageRect.top.toInt(),
                                    ),
                                )
                            }
                        }
                    }
                },
        )
    }
}

suspend fun renderToBitmap(
    context: Context,
    currentCanvasState: ExportImageCanvas,
): Bitmap? {
    return useVirtualDisplay(context) { display ->
        val density = Density(2f)
        val size = with (density) {
            val newSize = currentCanvasState.canvasSize * density.density
            newSize.toDpSize()
        }
        captureComposable(
            context = context,
            density = density,
            size = size,
            display = display
        ) {
            LaunchedEffect(Unit) {
                capture()
            }
            CustomizedImageRenderer(
                exportImageCanvas = currentCanvasState,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}