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
package com.android.developers.androidify.results

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.developers.androidify.data.ConnectedDevice
import com.android.developers.androidify.data.ImageGenerationRepository
import com.android.developers.androidify.data.WearAssetTransmitter
import com.android.developers.androidify.data.WearDeviceRepository
import com.android.developers.androidify.wear.common.WatchFaceInstallError
import com.android.developers.androidify.wear.common.WatchFaceInstallationStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ResultsViewModel @Inject constructor(
    val imageGenerationRepository: ImageGenerationRepository,
    val wearDeviceRepository: WearDeviceRepository,
    val wearAssetTransmitter: WearAssetTransmitter,
) : ViewModel() {

    private val _state = MutableStateFlow(ResultState())
    val state = combine(wearDeviceRepository.connectedDevice, _state) { device, state ->
        state.copy(connectedDevice = device)
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ResultState()
    )

    private var transferJob : Job? = null

    private var _snackbarHostState = MutableStateFlow(SnackbarHostState())

    val snackbarHostState: StateFlow<SnackbarHostState>
        get() = _snackbarHostState

    init {
        viewModelScope.launch {
            wearAssetTransmitter.receiveInstallUpdate().collect {
                _state.update { state ->
                    state.copy(installationStatus = it)
                }
            }
        }
    }

    fun setArguments(
        resultImageUrl: Bitmap,
        originalImageUrl: Uri?,
        promptText: String?,
    ) {
        _state.update {
            ResultState(
                resultImageUrl,
                originalImageUrl,
                promptText = promptText,
                connectedDevice = it.connectedDevice,
                installationStatus = it.installationStatus
            )
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

    fun downloadClicked() {
        viewModelScope.launch {
            val resultBitmap = state.value.resultImageBitmap
            val originalImage = state.value.originalImageUrl
            if (originalImage != null) {
                val savedOriginalUri = imageGenerationRepository.saveImageToExternalStorage(originalImage)
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

    fun installWatchFace() {
        transferJob = viewModelScope.launch {
            val nodeId = state.value.connectedDevice?.nodeId
            val resultBitmap = state.value.resultImageBitmap
            if (nodeId != null && resultBitmap != null) {
                _state.update { it.copy(installationStatus = WatchFaceInstallationStatus.Sending) }

                val response = wearAssetTransmitter.doTransfer(nodeId, File(""), "")

                if (response != WatchFaceInstallError.NO_ERROR) {
                    _state.update {
                        it.copy(installationStatus = WatchFaceInstallationStatus.Complete(
                            success = false,
                            installError = response,
                            otherNodeId = nodeId,
                        ))
                    }
                }
            }
        }
    }

    fun resetWatchFaceSend() {
        viewModelScope.launch {
            val nodeId = state.value.connectedDevice?.nodeId
            nodeId?.let {
                wearAssetTransmitter.sendCancel(it)
            }
        }
        transferJob?.cancel()
        transferJob = null
        _state.update { it.copy(installationStatus = WatchFaceInstallationStatus.NotStarted) }
    }
}

data class ResultState(
    val resultImageBitmap: Bitmap? = null,
    val originalImageUrl: Uri? = null,
    val savedUri: Uri? = null,
    val externalSavedUri: Uri? = null,
    val externalOriginalSavedUri: Uri? = null,
    val promptText: String? = null,
    val connectedDevice: ConnectedDevice? = null,
    val installationStatus: WatchFaceInstallationStatus = WatchFaceInstallationStatus.NotStarted
)
