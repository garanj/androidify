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

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.android.developers.androidify.watchface.WatchFaceAsset
import com.android.developers.androidify.wear.common.ConnectedDevice
import com.android.developers.androidify.wear.common.WatchFaceActivationStrategy
import com.android.developers.androidify.wear.common.WatchFaceInstallationStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchFaceModalSheet(
    connectedDevice: ConnectedDevice,
    onWatchFaceInstallClick: (String) -> Unit,
    installationStatus: WatchFaceInstallationStatus,
    sheetState: SheetState,
    isLoadingWatchFaces: Boolean,
    watchFaces: List<WatchFaceAsset>,
    selectedWatchFace: WatchFaceAsset?,
    onDismiss: () -> Unit,
    onLoad: () -> Unit,
    onWatchFaceSelect: (WatchFaceAsset) -> Unit
) {
    LaunchedEffect(Unit) {
        onLoad()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        when {
            connectedDevice.hasAndroidify -> {
                when (installationStatus) {
                    is WatchFaceInstallationStatus.Complete -> {
                        if (installationStatus.success) {
                            when (installationStatus.activationStrategy) {
                                WatchFaceActivationStrategy.LONG_PRESS_TO_SET -> {
                                    LongPressPanel()
                                }

                                WatchFaceActivationStrategy.FOLLOW_PROMPT_ON_WATCH -> {
                                    GrantPermissionsPanel()
                                }

                                WatchFaceActivationStrategy.NO_ACTION_NEEDED,
                                WatchFaceActivationStrategy.CALL_SET_ACTIVE_NO_USER_ACTION,
                                -> {
                                    AllDonePanel()
                                }

                                WatchFaceActivationStrategy.GO_TO_WATCH_SETTINGS -> {
                                    UpdateSettingsPanel()
                                }
                            }
                        } else {
                            ErrorPanel()
                        }
                    }
                    else -> {
                        InstallWatchFacePanel(
                            deviceName = connectedDevice.displayName,
                            isSendingToWatch = installationStatus is WatchFaceInstallationStatus.Sending,
                            onInstallClick = {
                                onWatchFaceInstallClick(connectedDevice.nodeId)
                            },
                            isLoadingWatchFaces = isLoadingWatchFaces,
                            watchFaces = watchFaces,
                            selectedWatchFace = selectedWatchFace,
                            onWatchFaceSelect = onWatchFaceSelect
                        )
                    }
                }
            }
            else -> {
                InstallAndroidifyPanel(deviceName = connectedDevice.displayName)
            }
        }
    }
}
