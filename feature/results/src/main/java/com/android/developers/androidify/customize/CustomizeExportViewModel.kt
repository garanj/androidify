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

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.developers.androidify.data.ImageGenerationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomizeExportViewModel @Inject constructor(
    val imageGenerationRepository: ImageGenerationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CustomizeExportState())
    val state = _state.asStateFlow()

    private var _snackbarHostState = MutableStateFlow(SnackbarHostState())

    val snackbarHostState: StateFlow<SnackbarHostState>
        get() = _snackbarHostState

    fun setArguments(
        resultImageUrl: Bitmap,
        originalImageUrl: Uri?,
    ) {
        _state.update {
            CustomizeExportState(resultImageUrl, originalImageUrl)
        }
    }

    fun shareClicked() {
        viewModelScope.launch {
            val resultUrl = state.value.resultImageBitmap
            if (resultUrl != null) {
                val imageFileUri = imageGenerationRepository.saveImage(resultUrl)

                _state.update {
                    it.copy(savedUri = imageFileUri)
                }
            }
        }
    }
    fun selectedToolStateChanged(toolState: ToolState) {
        _state.update {
            it.copy(toolState = it.toolState + (it.selectedTool to toolState))
        }
    }
    fun downloadClicked() {
        viewModelScope.launch {
            val resultBitmap = state.value.resultImageBitmap
            val originalImage = state.value.originalImageUrl
            if (originalImage != null) {
                val savedOriginalUri =
                    imageGenerationRepository.saveImageToExternalStorage(originalImage)
                _state.update {
                    it.copy(externalOriginalSavedUri = savedOriginalUri)
                }
            }
            if (resultBitmap != null) {
                val imageUri = imageGenerationRepository.saveImageToExternalStorage(resultBitmap)
                _state.update {
                    it.copy(externalSavedUri = imageUri)
                }
                snackbarHostState.value.showSnackbar("Download complete")
            }
        }
    }

    fun changeSelectedTool(tool: CustomizeTool) {
        _state.update {
            it.copy(selectedTool = tool)
        }
    }
}

data class CustomizeExportState(
    val resultImageBitmap: Bitmap? = null,
    val originalImageUrl: Uri? = null,
    val savedUri: Uri? = null,
    val externalSavedUri: Uri? = null,
    val externalOriginalSavedUri: Uri? = null,
    val selectedTool: CustomizeTool = CustomizeTool.Size,
    val tools: List<CustomizeTool> = CustomizeTool.entries,
    val toolState: Map<CustomizeTool, ToolState> = mapOf(
        CustomizeTool.Size to AspectRatioToolState(),
        CustomizeTool.Background to BackgroundToolState(),
    ),
) {
    val selectedAspectRatio: Float
        get() = (toolState[CustomizeTool.Size] as AspectRatioToolState).selectedToolOption.aspectRatio

    val selectedBackground: BackgroundOption
        get() = (toolState[CustomizeTool.Background] as BackgroundToolState).selectedToolOption
}
interface ToolState {
    val selectedToolOption: ToolOption
    val options: List<ToolOption>
}
data class AspectRatioToolState(
    override val selectedToolOption: SizeOption = SizeOption.Square,
    override val options: List<SizeOption> = listOf(
        SizeOption.Square,
        SizeOption.Banner,
        SizeOption.Wallpaper,
        SizeOption.Custom,
    ),
) : ToolState

data class BackgroundToolState(
    override val selectedToolOption: BackgroundOption = BackgroundOption.None,
    override val options: List<BackgroundOption> = listOf(
        BackgroundOption.None,
        BackgroundOption.Lightspeed,
        BackgroundOption.IO,
        BackgroundOption.Create,
    ),
) : ToolState

data class ExportImageCanvas(
    var aspectRatio: Float = 1f,
    var canvasSize: Size = Size(1000f, 1000f),
    var imageRect: Rect = Rect(offset = Offset.Zero, size = Size(1000f, 1000f)),
    var mainImageUri: Uri,
    var imageOriginalBitmapSize: Size? = Size(1024f, 1024f),
) {
    fun updateAspectRatio(newAspectRatio: Float, strategy: ImageScalingStrategy = ImageScalingStrategy.FILL): ExportImageCanvas {
        if (newAspectRatio <= 0f) {
            return this.copy()
        }
        val originalWidth = this.canvasSize.width
        val originalHeight = this.canvasSize.height
        var newCanvasWidth: Float
        var newCanvasHeight: Float

        if (newAspectRatio > this.aspectRatio) {
            newCanvasHeight = originalHeight
            newCanvasWidth = newCanvasHeight * newAspectRatio
        } else {
            newCanvasWidth = originalWidth
            newCanvasHeight = newCanvasWidth / newAspectRatio
        }

        val adjustedCanvasSize = Size(newCanvasWidth, newCanvasHeight)
        var newImageRect = this.imageRect
        if (imageOriginalBitmapSize != null) {
            newImageRect = when (strategy) {
                ImageScalingStrategy.FIT -> calculateImageRectToFit(imageOriginalBitmapSize!!, adjustedCanvasSize)
                ImageScalingStrategy.FILL -> calculateImageRectToFill(imageOriginalBitmapSize!!, adjustedCanvasSize) // You'd need a robust calculateImageRectToFill
            }
        }

        return this.copy(
            aspectRatio = newAspectRatio,
            canvasSize = adjustedCanvasSize,
            imageRect = newImageRect,
        )
    }

    fun calculateImageRectToFit(imageOriginalSize: Size, newCanvasSize: Size): Rect {
        val imageAspectRatio = imageOriginalSize.width / imageOriginalSize.height
        val canvasAspectRatio = newCanvasSize.width / newCanvasSize.height

        var rectWidth: Float
        var rectHeight: Float
        var offsetX: Float
        var offsetY: Float

        if (imageAspectRatio > canvasAspectRatio) {
            rectWidth = newCanvasSize.width
            rectHeight = rectWidth / imageAspectRatio
            offsetX = 0f
            offsetY = (newCanvasSize.height - rectHeight) / 2f
        } else {
            rectHeight = newCanvasSize.height
            rectWidth = rectHeight * imageAspectRatio
            offsetX = (newCanvasSize.width - rectWidth) / 2f
            offsetY = 0f
        }
        return Rect(Offset(offsetX, offsetY), Size(rectWidth, rectHeight))
    }

    fun calculateImageRectToFill(imageOriginalSize: Size, newCanvasSize: Size): Rect {
        val imageAspectRatio = imageOriginalSize.width / imageOriginalSize.height
        val canvasAspectRatio = newCanvasSize.width / newCanvasSize.height

        var rectWidth: Float
        var rectHeight: Float
        var offsetX = 0f
        var offsetY = 0f
        if (imageAspectRatio > canvasAspectRatio) {
            rectHeight = newCanvasSize.height
            rectWidth = rectHeight * imageAspectRatio
            offsetX = (newCanvasSize.width - rectWidth) / 2f
            offsetY = 0f
        } else {
            rectWidth = newCanvasSize.width
            rectHeight = rectWidth / imageAspectRatio
            offsetX = 0f
            offsetY = (newCanvasSize.height - rectHeight) / 2f
        }
        return Rect(Offset(offsetX, offsetY), Size(rectWidth, rectHeight))
    }
}
enum class ImageScalingStrategy {
    FIT,
    FILL,
}
