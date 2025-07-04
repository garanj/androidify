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
@file:OptIn(ExperimentalPermissionsApi::class)

package com.android.developers.androidify.customize

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.android.developers.androidify.results.PermissionRationaleDialog
import com.android.developers.androidify.results.R
import com.android.developers.androidify.results.shareImage
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.theme.components.AndroidifyTopAppBar
import com.android.developers.androidify.theme.components.PrimaryButton
import com.android.developers.androidify.theme.components.SecondaryOutlinedButton
import com.android.developers.androidify.util.AdaptivePreview
import com.android.developers.androidify.util.allowsFullContent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.android.developers.androidify.theme.R as ThemeR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeAndExportScreen(
    resultImage: Bitmap,
    originalImageUri: Uri?,
    onBackPress: () -> Unit,
    onInfoPress: () -> Unit,
    viewModel: CustomizeExportViewModel = hiltViewModel<CustomizeExportViewModel>(),
) {
    LaunchedEffect(resultImage, originalImageUri) {
        viewModel.setArguments(resultImage, originalImageUri)
    }
    val state = viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(state.value.savedUri) {
        val savedImageUri = state.value.savedUri
        if (savedImageUri != null) {
            shareImage(context, savedImageUri)
        }
    }
    CustomizeExportContents(
        state.value,
        onBackPress,
        onInfoPress,
        onToolSelected = { tool ->
            viewModel.changeSelectedTool(tool)
        },
        onShareClicked = viewModel::shareClicked,
        onDownloadClicked = viewModel::downloadClicked,
        onSelectedToolStateChanged = viewModel::selectedToolStateChanged,
        snackbarHostState = viewModel.snackbarHostState.collectAsStateWithLifecycle().value,
    )
}

@Composable
private fun CustomizeExportContents(
    state: CustomizeExportState,
    onBackPress: () -> Unit,
    onInfoPress: () -> Unit,
    onShareClicked: () -> Unit,
    onDownloadClicked: () -> Unit,
    onToolSelected: (CustomizeTool) -> Unit,
    onSelectedToolStateChanged: (ToolState) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    Snackbar(snackbarData, shape = SnackbarDefaults.shape)
                },
            )
        },
        topBar = {
            AndroidifyTopAppBar(
                backEnabled = true,
                onBackPressed = onBackPress,
                onAboutClicked = onInfoPress,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            ImageResult(
                state.resultImageBitmap,
                Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.height(16.dp))
            ToolSelector(
                tools = state.tools,
                selectedOption = state.selectedTool,
                onToolSelected = { tool ->
                    onToolSelected(tool)
                },
            )
            Spacer(modifier = Modifier.height(16.dp))
            SelectedToolDetail(
                state,
                onSelectedToolStateChanged = { toolState ->
                    onSelectedToolStateChanged(toolState)
                },
            )
            Spacer(modifier = Modifier.height(16.dp))
            BotActionsButtonRow(
                onShareClicked = {
                    onShareClicked()
                },
                onDownloadClicked = {
                    onDownloadClicked()
                },
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SelectedToolDetail(
    state: CustomizeExportState,
    onSelectedToolStateChanged: (ToolState) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(state.selectedTool, modifier = modifier) { targetState ->
        val toolState = state.toolState[targetState]
        when (targetState) {
            CustomizeTool.Size -> {
                val aspectRatioToolState = toolState as AspectRatioToolState
                AspectRatioTool(
                    aspectRatioToolState.options,
                    aspectRatioToolState.selectedToolOption,
                    {
                        onSelectedToolStateChanged(aspectRatioToolState.copy(selectedToolOption = it))
                    },
                )
            }

            CustomizeTool.Background -> {
                val backgroundToolState = toolState as BackgroundToolState
                BackgroundTool(
                    backgroundToolState.options,
                    backgroundToolState.selectedToolOption,
                    {
                        onSelectedToolStateChanged(backgroundToolState.copy(selectedToolOption = it))
                    },
                )
            }
        }
    }
}

@Composable
private fun ImageResult(
    bitmap: Bitmap?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(32.dp),
    ) {
        AsyncImage(
            model = bitmap,
            contentDescription = stringResource(R.string.resultant_android_bot),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
                .shadow(8.dp, shape = MaterialTheme.shapes.large)
                .clip(MaterialTheme.shapes.large),
        )
    }
}

@Composable
private fun BotActionsButtonRow(
    onShareClicked: () -> Unit,
    onDownloadClicked: () -> Unit,
    modifier: Modifier = Modifier,
    verboseLayout: Boolean = allowsFullContent(),
) {
    Row(modifier) {
        PrimaryButton(
            onClick = {
                onShareClicked()
            },
            leadingIcon = {
                Row {
                    Icon(
                        ImageVector
                            .vectorResource(ThemeR.drawable.sharp_share_24),
                        contentDescription = null, // decorative element
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            },
            buttonText = if (verboseLayout) stringResource(R.string.share_your_bot) else null,
        )
        Spacer(Modifier.width(8.dp))
        val externalStoragePermission = rememberPermissionState(
            permission = Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
        val mustGrantPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            false
        } else {
            !externalStoragePermission.status.isGranted
        }
        var showRationaleDialog by remember {
            mutableStateOf(false)
        }
        SecondaryOutlinedButton(
            onClick = {
                if (mustGrantPermission) {
                    if (externalStoragePermission.status.shouldShowRationale) {
                        showRationaleDialog = true
                    } else {
                        externalStoragePermission.launchPermissionRequest()
                    }
                    externalStoragePermission.launchPermissionRequest()
                } else {
                    onDownloadClicked()
                }
            },
            leadingIcon = {
                Icon(
                    ImageVector
                        .vectorResource(R.drawable.rounded_download_24),
                    contentDescription = stringResource(R.string.download_bot),
                )
            },
        )
        PermissionRationaleDialog(
            showRationaleDialog,
            onDismiss = {
                showRationaleDialog = false
            },
            externalStoragePermission,
        )
    }
}

@Preview(showBackground = true)
@AdaptivePreview
@Composable
fun CustomizeExportPreview() {
    AndroidifyTheme {
        val bitmap = ImageBitmap.imageResource(R.drawable.placeholderbot)
        val state = CustomizeExportState(resultImageBitmap = bitmap.asAndroidBitmap())
        CustomizeExportContents(
            state = state,
            onDownloadClicked = {},
            onShareClicked = {},
            onBackPress = {},
            onInfoPress = {},
            onToolSelected = {},
            snackbarHostState = SnackbarHostState(),
            onSelectedToolStateChanged = {},
        )
    }
}
