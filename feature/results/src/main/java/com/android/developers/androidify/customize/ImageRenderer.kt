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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import com.android.developers.androidify.results.R

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
                .aspectRatio(
                    exportImageCanvas.aspectRatioOption.aspectRatio,
                    matchHeightConstraintsFirst = true,
                ),
        ) {
            BackgroundLayout(
                exportImageCanvas,
                modifier = Modifier.fillMaxSize(),
            ) {
                if (exportImageCanvas.imageBitmap != null) {
                    Image(
                        bitmap = exportImageCanvas.imageBitmap.asImageBitmap(),
                        modifier = Modifier
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

@Composable
fun BackgroundLayout(
    exportImageCanvas: ExportImageCanvas,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (exportImageCanvas.selectedBackgroundDrawable != null) {
            Image(
                bitmap = ImageBitmap.imageResource(id = exportImageCanvas.selectedBackgroundDrawable),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                contentDescription = null,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layout { measurable, constraints ->
                    val actualWidth = constraints.maxWidth
                    val actualHeight = constraints.maxHeight

                    val scale = if (exportImageCanvas.canvasSize.width > 0f) {
                        actualWidth / exportImageCanvas.canvasSize.width
                    } else {
                        1f
                    }

                    val scaledImageWidth = exportImageCanvas.imageSize.width * scale
                    val scaledImageHeight = exportImageCanvas.imageSize.height * scale
                    val scaledOffsetX = exportImageCanvas.imageOffset.x * scale
                    val scaledOffsetY = exportImageCanvas.imageOffset.y * scale

                    val placeable = measurable.measure(
                        constraints.copy(
                            minWidth = scaledImageWidth.toInt(),
                            maxWidth = scaledImageWidth.toInt(),
                            minHeight = scaledImageHeight.toInt(),
                            maxHeight = scaledImageHeight.toInt(),
                        ),
                    )
                    layout(actualWidth, actualHeight) {
                        placeable.placeRelative(scaledOffsetX.toInt(), scaledOffsetY.toInt())
                    }
                }
                .rotate(exportImageCanvas.imageRotation),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(percent =
                        if (exportImageCanvas.selectedBackgroundOption == BackgroundOption.None)
                            0 else 6)),
                contentAlignment = Alignment.Center,
            ) {
                content()
            }
        }
    }
}

@Preview
@Composable
private fun ImageRendererPreviewSquare() {
    val bitmap = ImageBitmap.imageResource(R.drawable.placeholderbot)

    ImageResult(
        ExportImageCanvas(
            imageBitmap = bitmap.asAndroidBitmap(),
            canvasSize = Size(1000f, 1000f),
            aspectRatioOption = SizeOption.Square,
            selectedBackgroundOption = BackgroundOption.IO,
        )
            .updateAspectRatioAndBackground(
                backgroundOption = BackgroundOption.IO,
                sizeOption = SizeOption.Square,
            ),
        modifier = Modifier
            .fillMaxSize(),
    )
}

@Preview
@Composable
private fun ImageRendererPreviewBanner() {
    val bitmap = ImageBitmap.imageResource(R.drawable.placeholderbot)

    ImageResult(
        ExportImageCanvas(
            imageBitmap = bitmap.asAndroidBitmap(),
            canvasSize = Size(1000f, 1000f),
            aspectRatioOption = SizeOption.Banner,
            selectedBackgroundOption = BackgroundOption.Lightspeed,
        ).updateAspectRatioAndBackground(
            backgroundOption = BackgroundOption.Lightspeed,
            sizeOption = SizeOption.Banner,
        ),
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(SizeOption.Banner.aspectRatio),
    )
}

@Preview
@Composable
private fun ImageRendererPreviewWallpaper() {
    val bitmap = ImageBitmap.imageResource(R.drawable.placeholderbot)

    ImageResult(
        ExportImageCanvas(
            imageBitmap = bitmap.asAndroidBitmap(),
            canvasSize = Size(1000f, 1000f),
            aspectRatioOption = SizeOption.Wallpaper,
            selectedBackgroundOption = BackgroundOption.Lightspeed,
        ).updateAspectRatioAndBackground(
            backgroundOption = BackgroundOption.Lightspeed,
            sizeOption = SizeOption.Wallpaper,
        ),
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(SizeOption.Wallpaper.aspectRatio),
    )
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
private fun ImageRendererPreviewWallpaperTablet() {
    val bitmap = ImageBitmap.imageResource(R.drawable.placeholderbot)

    ImageResult(
        ExportImageCanvas(
            imageBitmap = bitmap.asAndroidBitmap(),
            canvasSize = Size(1280f, 800f),
            aspectRatioOption = SizeOption.WallpaperTablet,
            selectedBackgroundOption = BackgroundOption.Lightspeed,
        ).updateAspectRatioAndBackground(
            backgroundOption = BackgroundOption.Lightspeed,
            sizeOption = SizeOption.WallpaperTablet,
        ),
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(SizeOption.WallpaperTablet.aspectRatio),
    )
}

@Preview
@Composable
private fun ImageRendererPreviewWallpaperSocial() {
    val bitmap = ImageBitmap.imageResource(R.drawable.placeholderbot)

    ImageResult(
        ExportImageCanvas(
            imageBitmap = bitmap.asAndroidBitmap(),
            canvasSize = Size(1600f, 900f),
            aspectRatioOption = SizeOption.SocialHeader,
            selectedBackgroundOption = BackgroundOption.Lightspeed,
        ).updateAspectRatioAndBackground(
            backgroundOption = BackgroundOption.Lightspeed,
            sizeOption = SizeOption.SocialHeader,
        ),
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(SizeOption.SocialHeader.aspectRatio),
    )
}

@Preview
@Composable
fun ImageRendererPreviewWallpaperIO() {
    val bitmap = ImageBitmap.imageResource(R.drawable.placeholderbot)

    ImageResult(
        ExportImageCanvas(
            imageBitmap = bitmap.asAndroidBitmap(),
            canvasSize = Size(1600f, 900f),
            aspectRatioOption = SizeOption.SocialHeader,
            selectedBackgroundOption = BackgroundOption.IO,

        ).updateAspectRatioAndBackground(
            backgroundOption = BackgroundOption.IO,
            sizeOption = SizeOption.SocialHeader,
        ),
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(SizeOption.SocialHeader.aspectRatio),
    )
}
