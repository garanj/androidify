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
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.android.developers.androidify.customize

import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.android.developers.testing.data.TestFileProvider
import com.android.developers.testing.data.bitmapSample
import com.android.developers.testing.network.TestRemoteConfigDataSource
import com.android.developers.testing.repository.FakeImageGenerationRepository
import com.android.developers.testing.util.FakeComposableBitmapRenderer
import com.android.developers.testing.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class CustomizeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: CustomizeExportViewModel
    private val originalFakeUri = Uri.parse("content://com.example.app/images/original.jpg")

    private val fakeUri = Uri.parse("content://com.example.app/images/original.jpg")

    @Before
    fun setup() {
        val remoteConfigDataSource = TestRemoteConfigDataSource(true)
        remoteConfigDataSource.backgroundVibeEnabled = false
        viewModel = CustomizeExportViewModel(
            fakeUri,
            originalFakeUri,
            FakeImageGenerationRepository(),
            composableBitmapRenderer = FakeComposableBitmapRenderer(),
            application = ApplicationProvider.getApplicationContext(),
            localFileProvider = TestFileProvider(),
            remoteConfigDataSource = remoteConfigDataSource,
        )
    }

    @Test
    fun stateResultUri_NotNull() = runTest {
        assertNotNull(
            viewModel.state.value.exportImageCanvas.imageUri,
        )
    }

    @Test
    fun setArgumentsWithOriginalImage() = runTest {
        assertEquals(
            CustomizeExportState(
                exportImageCanvas = ExportImageCanvas(imageUri = fakeUri, imageBitmap = bitmapSample),
                originalImageUrl = originalFakeUri,
            ),
            viewModel.state.value,
        )
    }

    @Test
    fun setArgumentsWithPrompt() = runTest {
        val remoteConfigDataSource = TestRemoteConfigDataSource(true)
        remoteConfigDataSource.backgroundVibeEnabled = false
        val viewModel = CustomizeExportViewModel(
            fakeUri,
            null,
            FakeImageGenerationRepository(),
            composableBitmapRenderer = FakeComposableBitmapRenderer(),
            application = ApplicationProvider.getApplicationContext(),
            localFileProvider = TestFileProvider(),
            remoteConfigDataSource = remoteConfigDataSource,
        )
        assertEquals(
            CustomizeExportState(
                exportImageCanvas = ExportImageCanvas(imageUri = fakeUri, imageBitmap = bitmapSample),
                originalImageUrl = null,
            ),
            viewModel.state.value,
        )
    }

    @Test
    fun downloadClicked() = runTest {
        val values = mutableListOf<CustomizeExportState>()
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.state.collect {
                values.add(it)
            }
        }


        viewModel.downloadClicked()
        assertNotNull(values.last().externalOriginalSavedUri)
        assertEquals(
            originalFakeUri,
            values.last().externalOriginalSavedUri,
        )
    }

    @Test
    fun shareClicked() = runTest {
        val values = mutableListOf<CustomizeExportState>()
        // Launch collector on the backgroundScope directly to use runTest's scheduler
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.state.collect {
                values.add(it)
            }
        }
        advanceUntilIdle()
        viewModel.shareClicked()
        // Ensure all coroutines on the test scheduler complete
        advanceUntilIdle()
        assertNotNull(values.last().savedUri)
    }

    @Test
    fun changeBackground_NotNull() = runTest {
        val viewModel = CustomizeExportViewModel(
            fakeUri,
            null,
            FakeImageGenerationRepository(),
            composableBitmapRenderer = FakeComposableBitmapRenderer(),
            application = ApplicationProvider.getApplicationContext(),
            localFileProvider = TestFileProvider(),
            remoteConfigDataSource = TestRemoteConfigDataSource(false),
        )
        val values = mutableListOf<CustomizeExportState>()
        // Launch collector on the backgroundScope directly to use runTest's scheduler
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.state.collect {
                values.add(it)
            }
        }
        advanceUntilIdle()
        viewModel.selectedToolStateChanged(
            BackgroundToolState(
                selectedToolOption = BackgroundOption.Chef,
                options = listOf(
                    BackgroundOption.None,
                    BackgroundOption.IO,
                    BackgroundOption.Chef,
                ),
            ),
        )
        advanceUntilIdle()
        assertFalse { values[values.lastIndex].showImageEditProgress }
        // assertTrue(values.any { it.showImageEditProgress })
        assertNotNull(values.last().exportImageCanvas.imageWithEdit)
    }

    @Test
    fun changeBackground_None() = runTest {
        val values = mutableListOf<CustomizeExportState>()
        // Launch collector on the backgroundScope directly to use runTest's scheduler
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            viewModel.state.collect {
                values.add(it)
            }
        }
        advanceUntilIdle()
        viewModel.selectedToolStateChanged(
            BackgroundToolState(
                selectedToolOption = BackgroundOption.None,
                options = listOf(
                    BackgroundOption.None,
                    BackgroundOption.IO,
                    BackgroundOption.Chef,
                ),
            ),
        )
        advanceUntilIdle()
        assertTrue { !values[values.lastIndex].showImageEditProgress }
        assertNull(values.last().exportImageCanvas.imageWithEdit)
    }

    @Test
    fun remoteConfigDataSource_BackgroundVibesFeatureEnabled_ContainsVibeList() = runTest {
        val remoteConfigDataSource = TestRemoteConfigDataSource(true)
        remoteConfigDataSource.backgroundVibeEnabled = true
        val viewModel = CustomizeExportViewModel(
            fakeUri,
            null,
            FakeImageGenerationRepository(),
            composableBitmapRenderer = FakeComposableBitmapRenderer(),
            application = ApplicationProvider.getApplicationContext(),
            localFileProvider = TestFileProvider(),
            remoteConfigDataSource = remoteConfigDataSource,
        )
        val state = viewModel.state.value.toolState[CustomizeTool.Background] as BackgroundToolState

        assertTrue(state.options.size > 5)
        assertTrue(state.options.any { it.aiBackground })
    }
}
