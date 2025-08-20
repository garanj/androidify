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
package com.android.developers.androidify.watchface.transfer

import android.graphics.Bitmap
import com.android.developers.androidify.watchface.creator.WatchFaceCreator
import com.android.developers.androidify.wear.common.ConnectedDevice
import com.android.developers.androidify.wear.common.WatchFaceInstallError
import com.android.developers.androidify.wear.common.WatchFaceInstallationStatus
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

/**
 * Repository for watch face installation.
 */
interface WatchFaceInstallationRepository {
    /**
     * Flow of currently connected device. Only one device is reported - the scenario of having
     * multiple devices connected is not currently supported.
     */
    val connectedDevice: Flow<ConnectedDevice?>

    /**
     * Flow of status updates from the watch as installation proceeds.
     */
    val watchFaceInstallationUpdates: Flow<WatchFaceInstallationStatus>

    /**
     * Creates and transmits a watch face to the connected device. The bitmap is added into the
     * template watch face.
     *
     * @param connectedDevice The device to install the watch face on.
     * @param bitmap The bitmap to add to the watch face.
     * @return The result of the transfer.
     */
    suspend fun createAndTransferWatchFace(connectedDevice: ConnectedDevice, bitmap: Bitmap): WatchFaceInstallError
}

class WatchFaceInstallationRepositoryImpl @Inject constructor(
    private val wearAssetTransmitter: WearAssetTransmitter,
    private val wearDeviceRepository: WearDeviceRepository,
    private val watchFaceCreator: WatchFaceCreator,
) : WatchFaceInstallationRepository {
    override val connectedDevice = wearDeviceRepository.connectedDevice

    private val manualStatusUpdates = MutableSharedFlow<WatchFaceInstallationStatus>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val watchFaceInstallationUpdates: Flow<WatchFaceInstallationStatus> = merge(
        manualStatusUpdates,
        wearAssetTransmitter.watchFaceInstallationUpdates,
    )

    override suspend fun createAndTransferWatchFace(connectedDevice: ConnectedDevice, bitmap: Bitmap): WatchFaceInstallError {
        manualStatusUpdates.tryEmit(WatchFaceInstallationStatus.Sending)
        val watchFacePackage = watchFaceCreator.createWatchFacePackage(bitmap)

        return wearAssetTransmitter.doTransfer(connectedDevice.nodeId, watchFacePackage)
    }
}
