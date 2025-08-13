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

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.Modifier
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.developers.androidify.data.ImageGenerationRepository
import com.android.developers.androidify.data.WearAssetTransmitter
import com.android.developers.androidify.data.WearDeviceRepository
import com.android.developers.androidify.watchface.WatchFaceCreator
import com.android.developers.androidify.wear.common.WatchFaceInstallError
import com.android.developers.androidify.wear.common.WatchFaceInstallationStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CustomizeExportViewModel @Inject constructor(
    val imageGenerationRepository: ImageGenerationRepository,
    val composableBitmapRenderer: ComposableBitmapRenderer,
    val wearDeviceRepository: WearDeviceRepository,
    val wearAssetTransmitter: WearAssetTransmitter,
    val watchFaceCreator: WatchFaceCreator,
    application: Application,
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(CustomizeExportState())
    val state = _state.asStateFlow()

    private var transferJob : Job? = null

    val connectedDevice = wearDeviceRepository.connectedDevice.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = null
    )

    init {
        viewModelScope.launch {
            wearAssetTransmitter.receiveInstallUpdate().collect {
                _state.update { state ->
                    state.copy(installationStatus = it)
                }
            }
        }
    }

    private var _snackbarHostState = MutableStateFlow(SnackbarHostState())

    val snackbarHostState: StateFlow<SnackbarHostState>
        get() = _snackbarHostState

    override fun onCleared() {
        super.onCleared()
        composableBitmapRenderer.dispose()
    }
    fun setArguments(
        resultImageUrl: Bitmap,
        originalImageUrl: Uri?,
    ) {
        _state.update {
            CustomizeExportState(
                originalImageUrl,
                exportImageCanvas = it.exportImageCanvas.copy(imageBitmap = resultImageUrl),
            )
        }
    }

    fun shareClicked() {
        viewModelScope.launch {
            val exportImageCanvas = state.value.exportImageCanvas
            val resultBitmap = composableBitmapRenderer.renderComposableToBitmap(exportImageCanvas.canvasSize) {
                ImageResult(
                    exportImageCanvas = exportImageCanvas,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            if (resultBitmap != null) {
                val imageFileUri = imageGenerationRepository.saveImage(resultBitmap)

                _state.update {
                    it.copy(savedUri = imageFileUri)
                }
            }
        }
    }
    fun onSavedUriConsumed() {
        _state.update {
            it.copy(savedUri = null)
        }
    }
    fun selectedToolStateChanged(toolState: ToolState) {
        _state.update {
            it.copy(
                toolState = it.toolState + (it.selectedTool to toolState),
                exportImageCanvas =
                when (toolState.selectedToolOption) {
                    is BackgroundOption -> {
                        val backgroundOption = toolState.selectedToolOption as BackgroundOption
                        it.exportImageCanvas.updateAspectRatioAndBackground(
                            backgroundOption,
                            it.exportImageCanvas.aspectRatioOption,
                        )
                    }
                    is SizeOption -> {
                        it.exportImageCanvas.updateAspectRatioAndBackground(
                            it.exportImageCanvas.selectedBackgroundOption,
                            (toolState.selectedToolOption as SizeOption),
                        )
                    }
                    else -> throw IllegalArgumentException("Unknown tool option")
                },
            )
        }
    }

    fun downloadClicked() {
        viewModelScope.launch {
            val exportImageCanvas = state.value.exportImageCanvas
            val resultBitmap = composableBitmapRenderer.renderComposableToBitmap(exportImageCanvas.canvasSize) {
                ImageResult(
                    exportImageCanvas = exportImageCanvas,
                    modifier = Modifier.fillMaxSize(),
                )
            }
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
    fun installWatchFace() {
        transferJob = viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val nodeId = connectedDevice.value?.nodeId
                if (nodeId != null && state.value.exportImageCanvas.imageBitmap != null) {
                    _state.update { it.copy(installationStatus = WatchFaceInstallationStatus.Sending) }
                    val wfBitmap = state.value.exportImageCanvas.imageBitmap
                    val watchFacePackage = watchFaceCreator.createWatchFacePackage(wfBitmap!!)

                    val response = wearAssetTransmitter.doTransfer(nodeId, watchFacePackage)

                    if (response != WatchFaceInstallError.NO_ERROR) {
                        _state.update {
                            it.copy(
                                installationStatus = WatchFaceInstallationStatus.Complete(
                                    success = false,
                                    installError = response,
                                    otherNodeId = nodeId,
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun resetWatchFaceSend() {
        transferJob?.cancel()
        transferJob = null
        _state.update { it.copy(installationStatus = WatchFaceInstallationStatus.NotStarted) }
    }
}
