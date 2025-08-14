package com.android.developers.androidify.customize

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import com.android.developers.androidify.results.AllDonePanel
import com.android.developers.androidify.results.ErrorPanel
import com.android.developers.androidify.results.GrantPermissionsPanel
import com.android.developers.androidify.results.InstallAndroidifyPanel
import com.android.developers.androidify.results.InstallWatchFacePanel
import com.android.developers.androidify.results.LongPressPanel
import com.android.developers.androidify.results.UpdateSettingsPanel
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
    onDismiss: () -> Unit,
) {
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
                                WatchFaceActivationStrategy.CALL_SET_ACTIVE_NO_USER_ACTION
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
                            onButtonClick = {
                                onWatchFaceInstallClick(connectedDevice.nodeId)
                            },
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
