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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.toIntSize
import com.android.developers.androidify.results.R
import com.android.developers.androidify.util.LargeScreensPreview
import com.android.developers.androidify.util.PhonePreview

@Composable
fun ImageResult(
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
                                    image = exportImageCanvas.selectedBackground!!.asImageBitmap(),
                                )
                            }
                            if (exportImageCanvas.imageBitmap != null) {
                                drawImage(
                                    image = exportImageCanvas.imageBitmap.asImageBitmap(),
                                    srcOffset = IntOffset.Zero,
                                    srcSize = exportImageCanvas.imageOriginalBitmapSize!!.toIntSize(),
                                    dstOffset = IntOffset(
                                        exportImageCanvas.imageRect.left.toInt(),
                                        exportImageCanvas.imageRect.top.toInt(),
                                    ),
                                    dstSize = exportImageCanvas.imageRect.size.toIntSize(),
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
            imageRect = androidx.compose.ui.geometry.Rect(
                left = 0f,
                top = 0f,
                right = 100f,
                bottom = 100f,
            ),
            canvasSize = androidx.compose.ui.geometry.Size(100f, 100f),
            aspectRatio = 9 / 16f,
            selectedBackground = null,
        ),
        modifier = Modifier
            .fillMaxSize(),
    )
}
