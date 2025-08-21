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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.results.R
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.theme.components.PrimaryButton
import com.android.developers.androidify.watchface.WatchFaceAsset

@Composable
fun InstallWatchFacePanel(
    modifier: Modifier = Modifier,
    deviceName: String,
    isSendingToWatch: Boolean,
    isLoadingWatchFaces: Boolean,
    watchFaces: List<WatchFaceAsset>,
    selectedWatchFace: WatchFaceAsset?,
    onWatchFaceSelect: (WatchFaceAsset) -> Unit,
    onInstallClick: () -> Unit = { },
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.send_to_watch_cta, deviceName),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (isLoadingWatchFaces) {
            CircularProgressIndicator()
        } else if (watchFaces.isEmpty()) {
            Text(
                text = stringResource(R.string.no_watch_faces),
                color = MaterialTheme.colorScheme.error,
            )
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) {
                items(watchFaces, key = { it.id }) { watchFace ->
                    WatchFacePreviewItem(
                        watchFace = watchFace,
                        isSelected = watchFace.id == selectedWatchFace?.id,
                        onClick = {
                            onWatchFaceSelect(watchFace)
                        },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        PrimaryButton(
            onClick = onInstallClick,
            loading = isSendingToWatch,
            leadingIcon = {
                Row {
                    Icon(
                        ImageVector.vectorResource(R.drawable.watch_24),
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            },
            buttonText = stringResource(R.string.send_to_watch),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun InstallWatchFacePanelPreview() {
    val watchFace1 = WatchFaceAsset(
        id = "watch_face_1",
        previewPath = R.drawable.watch_face_preview,
    )
    val watchFace2 = WatchFaceAsset(
        id = "watch_face_2",
        previewPath = R.drawable.watch_face_preview,
    )
    AndroidifyTheme {
        InstallWatchFacePanel(
            deviceName = "Pixel 3",
            isSendingToWatch = false,
            isLoadingWatchFaces = false,
            watchFaces = listOf(watchFace1, watchFace2),
            selectedWatchFace = watchFace1,
            onWatchFaceSelect = {},
            onInstallClick = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun InstallWatchFacePanelNoWatchFacesPreview() {
AndroidifyTheme {
        InstallWatchFacePanel(
            deviceName = "Pixel 3",
            isSendingToWatch = false,
            isLoadingWatchFaces = false,
            watchFaces = listOf(),
            selectedWatchFace = null,
            onWatchFaceSelect = {},
            onInstallClick = {},
        )
    }
}
