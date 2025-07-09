package com.android.developers.androidify.customize

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size

data class CustomizeExportState(
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
    val exportImageCanvas: ExportImageCanvas = ExportImageCanvas()
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
    val imageBitmap: Bitmap? = null,
    var aspectRatio: Float = 1f,
    var canvasSize: Size = Size(1000f, 1000f),
    var imageRect: Rect = Rect(offset = Offset.Zero, size = Size(1000f, 1000f)),
    var mainImageUri: Uri? = null,
    var imageOriginalBitmapSize: Size? = Size(1024f, 1024f),
    var selectedBackground: Bitmap? = null,
) {

    fun scaleImage(percentage: Float): ExportImageCanvas {
        return this.copy(
            imageRect = Rect(
                offset = Offset(this.imageRect.left, this.imageRect.top),
                size = Size(
                    this.imageRect.size.width * percentage,
                    this.imageRect.size.height * percentage
                )
            )
        )
    }

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
