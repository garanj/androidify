/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.developers.androidify.customize

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.results.R
import com.android.developers.androidify.util.LargeScreensPreview
import com.android.developers.androidify.util.PhonePreview
import com.android.developers.androidify.util.dpToPx
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ImageResult(
    exportImageCanvas: ExportImageCanvas,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .background(Color.White)
                .aspectRatio(exportImageCanvas.aspectRatioOption.aspectRatio, matchHeightConstraintsFirst = true),
        ) {
            BackgroundLayout(exportImageCanvas.aspectRatioOption,
                exportImageCanvas.selectedBackgroundOption,
                modifier = Modifier.fillMaxSize()) {
                if (exportImageCanvas.imageBitmap  != null) {
                    Image(bitmap = exportImageCanvas.imageBitmap.asImageBitmap(),
                        modifier = Modifier
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        contentDescription = null)
                }
            }
        }
    }
}
@Composable
fun BackgroundLayout(
    sizeOption: SizeOption,
    backgroundOption: BackgroundOption,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
){
    Box(modifier = modifier.fillMaxSize()) {

        when (sizeOption) {
            SizeOption.Banner -> {
                // Background image for the banner
                val image = when (backgroundOption) {
                    BackgroundOption.IO -> R.drawable.background_banner_square
                    BackgroundOption.Lightspeed -> R.drawable.background_banner_lightspeed
                    BackgroundOption.None -> R.drawable.background_banner_plain
                }
                Image(
                    bitmap = ImageBitmap.imageResource(id = image),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    contentDescription = null
                )
                val whiteBoxXFraction = 0.51f
                val whiteBoxYFraction = 0.31f
                val whiteBoxWidthFraction = 0.23f
                val whiteBoxHeightFraction = 0.35f

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .layout { measurable, constraints ->
                            val fullWidth = constraints.maxWidth
                            val fullHeight = constraints.maxHeight

                            val whiteBoxWidth = (fullWidth * whiteBoxWidthFraction).roundToInt()
                            val whiteBoxHeight = (fullHeight * whiteBoxHeightFraction).roundToInt()

                            val whiteBoxX = (fullWidth * whiteBoxXFraction).roundToInt()
                            val whiteBoxY = (fullHeight * whiteBoxYFraction).roundToInt()

                            val placeable = measurable.measure(
                                constraints.copy(
                                    minWidth = whiteBoxWidth,
                                    maxWidth = whiteBoxWidth,
                                    minHeight = whiteBoxHeight,
                                    maxHeight = whiteBoxHeight
                                )
                            )
                            layout(fullWidth, fullHeight) {
                                placeable.placeRelative(whiteBoxX, whiteBoxY)
                            }
                        }
                        .aspectRatio(0.88f)
                        .rotate(-11f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(32.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        content()
                    }
                }
            }
            SizeOption.Square -> {
                var botOffset by remember { mutableStateOf(IntOffset.Zero) }
                botOffset = IntOffset(26.dp.dpToPx().roundToInt(), -28.dp.dpToPx().roundToInt())
                val image = when (backgroundOption) {
                    BackgroundOption.IO -> {
                        R.drawable.background_square_shape
                    }
                    BackgroundOption.Lightspeed -> {
                        R.drawable.background_square_lightspeed
                    }
                    BackgroundOption.None -> R.drawable.background_square_none
                }
                Image(ImageBitmap.imageResource(id = image),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = null)
                Box(modifier = Modifier
                    .offset{ botOffset }
                    .aspectRatio(0.88f)
                    .fillMaxSize()
                    .rotate(2f)
                    .scale(0.7f)
                    .clip(RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    content()
                }
            }
            SizeOption.Wallpaper -> {
                var botOffset by remember { mutableStateOf(IntOffset.Zero) }
                Box(modifier = Modifier
                    .offset{ botOffset }
                    .aspectRatio(0.88f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(32.dp))
                ) {
                    content()
                }
            }
        }

    }
}

suspend fun renderToBitmap(
    context: Context,
    currentCanvasState: ExportImageCanvas,
): Bitmap? {
    return useVirtualDisplay(context) { display ->
        val density = Density(2f)
        val size = with(density) {
            val newSize = currentCanvasState.canvasSize * density.density
            newSize.toDpSize()
        }
        captureComposable(
            context = context,
            density = density,
            size = size,
            display = display,
        ) {
            LaunchedEffect(Unit) {
                capture()
            }
            ImageResult(
                exportImageCanvas = currentCanvasState,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@PhonePreview
@LargeScreensPreview
@Composable
private fun ImageRendererPreview() {
    val bitmap = ImageBitmap.imageResource(R.drawable.placeholderbot)

    ImageResult(
        ExportImageCanvas(
            imageBitmap = bitmap.asAndroidBitmap(),
            canvasSize = androidx.compose.ui.geometry.Size(1000f, 1000f),
            aspectRatioOption = SizeOption.Square,
            selectedBackgroundOption = BackgroundOption.IO,
        ),
        modifier = Modifier
            .fillMaxSize()
           ,
    )
}
@PhonePreview
@LargeScreensPreview
@Composable
private fun ImageRendererPreviewBanner() {
    val bitmap = ImageBitmap.imageResource(R.drawable.placeholderbot)

    ImageResult(
        ExportImageCanvas(
            imageBitmap = bitmap.asAndroidBitmap(),
            canvasSize = androidx.compose.ui.geometry.Size(1000f, 1000f),
            aspectRatioOption = SizeOption.Banner,
            selectedBackgroundOption = BackgroundOption.Lightspeed,
        ),
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(SizeOption.Banner.aspectRatio)
        ,
    )

}
