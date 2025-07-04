package com.android.developers.androidify.customize

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.material3.SnackbarHostState
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

/*
* Copyright 2022 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
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
}

data class CustomizeExportState(
    val resultImageBitmap: Bitmap? = null,
    val originalImageUrl: Uri? = null,
    val savedUri: Uri? = null,
    val externalSavedUri: Uri? = null,
    val externalOriginalSavedUri: Uri? = null,
)