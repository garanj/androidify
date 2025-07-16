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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.animateSizeAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
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
                .layoutAspectRatio(exportImageCanvas.aspectRatioOption.aspectRatio)
            /*.aspectRatio(
                    exportImageCanvas.aspectRatioOption.aspectRatio,
                    matchHeightConstraintsFirst = true,
                ),*/
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

        val offset by animateOffsetAsState(
            targetValue = exportImageCanvas.imageOffset, label = "offset",
        )
        val animatedImageSize by animateSizeAsState(
            targetValue = exportImageCanvas.imageSize, label = "imageSize",
        )
        val rotationAnimation by animateFloatAsState(
            targetValue = exportImageCanvas.imageRotation, label = "rotation",
        )
        val exportCanvasSizeAnimation by animateSizeAsState(
            targetValue = exportImageCanvas.canvasSize, label = "canvas size",
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .layout { measurable, constraints ->
                    val offsetValue = if (!isLookingAhead) exportImageCanvas.imageOffset else offset
                    val imageSizeValue =
                        if (!isLookingAhead) exportImageCanvas.imageSize else animatedImageSize
                    val exportCanvasSizeAnimation =
                        if (!isLookingAhead) exportImageCanvas.canvasSize else exportCanvasSizeAnimation

                    val actualWidth = constraints.maxWidth
                    val actualHeight = constraints.maxHeight

                    val scale = if (exportCanvasSizeAnimation.width > 0f) {
                        actualWidth / exportCanvasSizeAnimation.width
                    } else {
                        1f
                    }

                    val scaledImageWidth = imageSizeValue.width * scale
                    val scaledImageHeight = imageSizeValue.height * scale
                    val scaledOffsetX = offsetValue.x * scale
                    val scaledOffsetY = offsetValue.y * scale

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
                .rotate(rotationAnimation),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(
                        RoundedCornerShape(
                            percent =
                                if (exportImageCanvas.selectedBackgroundOption == BackgroundOption.None)
                                    0 else 6,
                        ),
                    ),
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


fun Modifier.layoutAspectRatio(targetAspectRatio: Float)  = // Assume 'targetAspectRatio' is a Float value available in your scope (e.g., width / height)

    this.layout { measurable, constraints ->
        // Determine the ideal size for this layout based on the targetAspectRatio and incoming constraints.
        // This logic prioritizes fitting the width constraints first.
        var idealWidth: Int
        var idealHeight: Int

        if (targetAspectRatio <= 0f) {
            // Invalid aspect ratio, fallback to min constraints or 0,0
            idealWidth = constraints.minWidth
            idealHeight = constraints.minHeight
        } else {
            // Try to determine size based on width constraints
            if (constraints.hasFixedWidth) {
                idealWidth = constraints.maxWidth
                idealHeight = (idealWidth / targetAspectRatio).toInt()
            } else if (constraints.hasFixedHeight) { // Width is not fixed, but height is
                idealHeight = constraints.maxHeight
                idealWidth = (idealHeight * targetAspectRatio).toInt()
            } else {
                // Neither width nor height is fixed. Try to use maxWidth if bounded.
                if (constraints.maxWidth != Constraints.Infinity) {
                    idealWidth = constraints.maxWidth
                    idealHeight = (idealWidth / targetAspectRatio).toInt()

                    // If calculated height violates maxHeight (and maxHeight is bounded),
                    // then recalculate based on maxHeight.
                    if (constraints.maxHeight != Constraints.Infinity && idealHeight > constraints.maxHeight) {
                        idealHeight = constraints.maxHeight
                        idealWidth = (idealHeight * targetAspectRatio).toInt()
                    } else if (idealHeight < constraints.minHeight && constraints.minHeight != 0) {
                        // If calculated height is less than minHeight, recalculate based on minHeight.
                        idealHeight = constraints.minHeight
                        idealWidth = (idealHeight * targetAspectRatio).toInt()
                    }
                } else if (constraints.maxHeight != Constraints.Infinity) {
                    // Width is unbounded (Infinity), but maxHeight is bounded.
                    idealHeight = constraints.maxHeight
                    idealWidth = (idealHeight * targetAspectRatio).toInt()
                } else {
                    // Both width and height are unbounded (Infinity). Fallback to minConstraints.
                    // This case is ambiguous without further rules.
                    idealWidth = constraints.minWidth
                    idealHeight = (idealWidth / targetAspectRatio).toInt()
                    if (idealHeight < constraints.minHeight && constraints.minHeight != 0) {
                        idealHeight = constraints.minHeight
                        idealWidth = (idealHeight * targetAspectRatio).toInt()
                    }

                }
            }
        }

        // Coerce the ideal dimensions to ensure they fit within the provided constraints.
        // The layout itself will take this size.
        val layoutWidth = idealWidth.coerceIn(constraints.minWidth, constraints.maxWidth)

        // Recalculate layoutHeight based on the (potentially coerced) layoutWidth to maintain aspect ratio,
        // then coerce layoutHeight.
        var layoutHeight = if (targetAspectRatio > 0f) (layoutWidth / targetAspectRatio).toInt() else idealHeight
        layoutHeight = layoutHeight.coerceIn(constraints.minHeight, constraints.maxHeight)

        // Final adjustment: if layoutHeight was coerced, layoutWidth might need to be re-calculated
        // to maintain aspect ratio again, and then re-coerced. This iterative refinement helps
        // find the largest size that fits constraints AND maintains the aspect ratio.
        val adjustedLayoutWidth = if (targetAspectRatio > 0f) (layoutHeight * targetAspectRatio).toInt() else layoutWidth
        val finalLayoutWidth = adjustedLayoutWidth.coerceIn(constraints.minWidth, constraints.maxWidth)

        // Calculate the final height based on the truly final width.
        val finalLayoutHeight = if (targetAspectRatio > 0f) (finalLayoutWidth / targetAspectRatio).toInt() else layoutHeight
        // Coerce this final height one last time.
        val finalLayoutHeightCoerced = finalLayoutHeight.coerceIn(constraints.minHeight, constraints.maxHeight)

        // Measure the child composable (if any) to fill the dimensions determined for this layout.
        val placeable = measurable.measure(
            Constraints.fixed(finalLayoutWidth, finalLayoutHeightCoerced),
        )

        // Set the size of this custom layout to the calculated final dimensions
        // and place the child within it.
        layout(finalLayoutWidth, finalLayoutHeightCoerced) {
            placeable.placeRelative(0, 0)
        }
    }
