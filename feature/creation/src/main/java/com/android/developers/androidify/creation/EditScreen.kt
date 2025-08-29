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
@file:OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class,
)

package com.android.developers.androidify.creation

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.data.DropBehaviourFactory
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.theme.SharedElementContextPreview
import com.android.developers.androidify.theme.components.AboutButton
import com.android.developers.androidify.theme.components.AndroidifyTopAppBar
import com.android.developers.androidify.theme.components.SquiggleBackground
import com.android.developers.androidify.util.AdaptivePreview
import com.android.developers.androidify.util.isAtLeastMedium

@Composable
fun EditScreen(
    snackbarHostState: SnackbarHostState,
    dropBehaviourFactory: DropBehaviourFactory,
    isExpanded: Boolean = isAtLeastMedium(),
    onCameraPressed: () -> Unit,
    onBackPressed: () -> Unit,
    onAboutPressed: () -> Unit,
    uiState: CreationState,
    onChooseImageClicked: (PickVisualMedia.VisualMediaType) -> Unit,
    onPromptOptionSelected: (PromptType) -> Unit,
    onUndoPressed: () -> Unit,
    onPromptGenerationPressed: () -> Unit,
    onBotColorSelected: (BotColor) -> Unit,
    onStartClicked: () -> Unit,
    onDropCallback: (Uri) -> Unit = {},
) {
    EditScreenScaffold(
        snackbarHostState,
        topBar = {
            AndroidifyTopAppBar(
                backEnabled = true,
                isMediumWindowSize = isExpanded,
                onBackPressed = onBackPressed,
                expandedCenterButtons = {
                    PromptTypeToolbar(
                        uiState.selectedPromptOption,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                        onOptionSelected = onPromptOptionSelected,
                    )
                },
                actions = {
                    AboutButton { onAboutPressed() }
                }
            )
        },
    ) { contentPadding ->
        SquiggleBackground(offsetHeightFraction = 0.5f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .imePadding(),
        ) {
            if (isExpanded) {
                EditScreenContentsMedium(
                    dropBehaviourFactory,
                    onCameraPressed,
                    uiState,
                    onChooseImageClicked,
                    onPromptOptionSelected,
                    onUndoPressed,
                    onPromptGenerationPressed,
                    onBotColorSelected,
                    onStartClicked,
                    onDropCallback,
                )
            } else {
                EditScreenContentsCompact(
                    dropBehaviourFactory,
                    onCameraPressed,
                    uiState,
                    onChooseImageClicked,
                    onPromptOptionSelected,
                    onUndoPressed,
                    onPromptGenerationPressed,
                    onBotColorSelected,
                    onStartClicked,
                    onDropCallback,
                )
            }
        }
    }
}

@Composable
fun EditScreenScaffold(
    snackbarHostState: SnackbarHostState,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    Snackbar(
                        snackbarData,
                        shape = SnackbarDefaults.shape,
                        modifier = Modifier.padding(4.dp),
                    )
                },
                modifier = Modifier.safeContentPadding(),
            )
        },
        topBar = topBar,
        content = content,
    )
}

@Composable
@AdaptivePreview
private fun EditScreenPreview() {
    AndroidifyTheme {
        SharedElementContextPreview {
            EditScreen(
                snackbarHostState = SnackbarHostState(),
                dropBehaviourFactory = fakeDropBehaviourFactory,
                onCameraPressed = { },
                uiState = CreationState(),
                onChooseImageClicked = {},
                onPromptOptionSelected = {},
                onUndoPressed = {},
                onPromptGenerationPressed = {},
                onBotColorSelected = {},
                onStartClicked = {},
                onDropCallback = {},
                onBackPressed = {},
                onAboutPressed = {},
            )
        }
    }
}

val fakeDropBehaviourFactory = object : DropBehaviourFactory {
    override fun shouldStartDragAndDrop(event: DragAndDropEvent): Boolean {
        TODO("Stub")
    }

    override fun createTargetCallback(
        activity: ComponentActivity,
        onImageDropped: (Uri) -> Unit,
        onDropStarted: () -> Unit,
        onDropEnded: () -> Unit,
    ): DragAndDropTarget {
        TODO("Stub")
    }
}
