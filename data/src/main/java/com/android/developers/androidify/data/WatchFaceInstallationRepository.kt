package com.android.developers.androidify.data

import android.graphics.Bitmap
import com.android.developers.androidify.watchface.WatchFaceCreator
import com.android.developers.androidify.wear.common.ConnectedDevice
import com.android.developers.androidify.wear.common.WatchFaceInstallError
import com.android.developers.androidify.wear.common.WatchFaceInstallationStatus
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

interface WatchFaceInstallationRepository {
    val connectedDevice: Flow<ConnectedDevice?>
    val watchFaceInstallationUpdates: Flow<WatchFaceInstallationStatus>
    suspend fun createAndTransferWatchFace(connectedDevice: ConnectedDevice, bitmap: Bitmap): WatchFaceInstallError
}

class WatchFaceInstallationRepositoryImpl @Inject constructor(
    private val wearAssetTransmitter: WearAssetTransmitter,
    private val wearDeviceRepository: WearDeviceRepository,
    private val watchFaceCreator: WatchFaceCreator
): WatchFaceInstallationRepository {
    override val connectedDevice = wearDeviceRepository.connectedDevice

    private val manualStatusUpdates = MutableSharedFlow<WatchFaceInstallationStatus>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val watchFaceInstallationUpdates: Flow<WatchFaceInstallationStatus> = merge(
        manualStatusUpdates,
        wearAssetTransmitter.watchFaceInstallationUpdates
    )

    override suspend fun createAndTransferWatchFace(connectedDevice: ConnectedDevice, bitmap: Bitmap): WatchFaceInstallError {
        manualStatusUpdates.tryEmit(WatchFaceInstallationStatus.Sending)
        val watchFacePackage = watchFaceCreator.createWatchFacePackage(bitmap)

        return wearAssetTransmitter.doTransfer(connectedDevice.nodeId, watchFacePackage)
    }
}