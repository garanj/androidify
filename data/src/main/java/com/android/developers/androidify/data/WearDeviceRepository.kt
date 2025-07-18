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
@file:OptIn(ExperimentalSerializationApi::class)

package com.android.developers.androidify.data

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.android.developers.androidify.wear.common.WatchFaceActivationStrategy
import com.android.developers.androidify.wear.common.WearableConstants.ANDROIDIFY_APK_KEY
import com.android.developers.androidify.wear.common.WearableConstants.ANDROIDIFY_DATA_PATH_TEMPLATE
import com.android.developers.androidify.wear.common.WearableConstants.ANDROIDIFY_GUIDANCE_LAUNCH
import com.android.developers.androidify.wear.common.WearableConstants.ANDROIDIFY_INSTALLED
import com.android.developers.androidify.wear.common.WearableConstants.ANDROIDIFY_VALIDATION_TOKEN_KEY
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class ConnectedDevice(
    val nodeId: String,
    val displayName: String,
    val hasAndroidify: Boolean,
)

interface WearDeviceRepository {
    val connectedDevice: Flow<ConnectedDevice?>

    suspend fun sendAssetAndAwaitResponse(nodeId: String, file: File, timeoutMillis: Long = 60_000L): WatchFaceActivationStrategy
    suspend fun sendLaunch(nodeId: String)

}

@Singleton
class WearDeviceRepositoryImpl @Inject constructor(
    @ApplicationContext val context: Context,
) : WearDeviceRepository {
    private val nodeClient: NodeClient by lazy { Wearable.getNodeClient(context) }
    private val messageClient: MessageClient by lazy { Wearable.getMessageClient(context) }
    private val dataClient: DataClient by lazy { Wearable.getDataClient(context) }
    private val capabilityClient: CapabilityClient by lazy { Wearable.getCapabilityClient(context) }

    private val scope = CoroutineScope(Dispatchers.IO)

    override val connectedDevice = callbackFlow {
        val allDevices = nodeClient.connectedNodes.await().toSet()
        val reachableCapability =
            capabilityClient.getCapability(ANDROIDIFY_INSTALLED, CapabilityClient.FILTER_REACHABLE)
                .await()

        val installedDevices = reachableCapability.nodes.toSet()

        trySend(selectConnectedDevice(installedDevices, allDevices))

        val capabilityListener = CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
            val installedDevicesUpdated = capabilityInfo.nodes.toSet()

            trySend(selectConnectedDevice(installedDevicesUpdated, allDevices))
        }
        capabilityClient.addListener(capabilityListener, ANDROIDIFY_INSTALLED)
        awaitClose {
            capabilityClient.removeListener(capabilityListener)
        }
    }

    /**
     * Selects a [ConnectedDevice] if one is available, prioritizing devices with Androidify.
     * Returns null where no device at all is available.
     */
    private fun selectConnectedDevice(
        installedDevices: Set<Node>,
        allDevices: Set<Node>,
    ): ConnectedDevice? {
        return if (installedDevices.isNotEmpty()) {
            ConnectedDevice(
                nodeId = installedDevices.first().id,
                displayName = installedDevices.first().displayName,
                hasAndroidify = true,
            )
        } else if (allDevices.isNotEmpty()) {
            ConnectedDevice(
                nodeId = allDevices.first().id,
                displayName = allDevices.first().displayName,
                hasAndroidify = false,
            )
        } else {
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun sendAndroidifyApk(nodeId: String, file: File, token: String): Boolean {
        // TODO: Replace with actual generated APK
        val wf = context.assets.open("dummy.apk").readAllBytes()
        val asset = Asset.createFromBytes(wf)
        val nodeBasedPath = ANDROIDIFY_DATA_PATH_TEMPLATE.format(nodeId)

        val putDataMapRequest = PutDataMapRequest.create(nodeBasedPath)
        putDataMapRequest.dataMap.putAsset(ANDROIDIFY_APK_KEY, asset)
        // TODO Remove - token for the dummy APK
        putDataMapRequest.dataMap.putString(ANDROIDIFY_VALIDATION_TOKEN_KEY, "V3tEuE1H1de1Hx98KHDjlxP/LuWivLBn4o7CGRx/emI=:MS4wLjA=")
        // TODO Remove - forces refresh
        putDataMapRequest.dataMap.putLong("timestamp", System.currentTimeMillis())

        val putDataRequest = putDataMapRequest.asPutDataRequest().setUrgent()

        try {
            withContext(Dispatchers.IO) {
                withTimeout(SEND_TIMEOUT_MS) {
                    dataClient.putDataItem(putDataRequest).await()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error sending Androidify APK.", e)
            return false
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun sendAssetAndAwaitResponse(nodeId: String, file: File, timeoutMillis: Long): WatchFaceActivationStrategy {
//        val strategy = withTimeoutOrNull(timeoutMillis) {
//            suspendCancellableCoroutine<WatchFaceActivationStrategy> { continuation ->
//                val listener = MessageClient.OnMessageReceivedListener { messageEvent: MessageEvent ->
//                    if (messageEvent.path == ANDROIDIFY_APK_RECEIPT_PATH) {
//                        val state = ProtoBuf.decodeFromByteArray<WatchFacePushDeviceState>(messageEvent.data)
//                        val strategy = WatchFaceActivationStrategy.fromState(state)
//
//                        if (continuation.isActive) {
//                            continuation.resume(strategy)
//                        }
//                    }
//                }
//
//                continuation.invokeOnCancellation {
//                    messageClient.removeListener(listener)
//                }
//
//                messageClient.addListener(listener)
//                    .addOnSuccessListener {
//                        scope.launch {
//                            try {
//                                // TODO: populate this
//                                sendAndroidifyApk(nodeId, file, "TODO")
//                            } catch (e: Exception) {
//                                if (continuation.isActive) {
//                                    continuation.resume(WatchFaceActivationStrategy.TRANSMISSION_FAILURE)
//                                }
//                            }
//                        }
//                    }
//                    .addOnFailureListener { e ->
//                        messageClient.removeListener(listener)
//                        if (continuation.isActive) {
//                            continuation.resume(WatchFaceActivationStrategy.TRANSMISSION_FAILURE)
//                        }
//                    }
//            }
//        } ?: WatchFaceActivationStrategy.TRANSMISSION_FAILURE
//        return strategy
        return WatchFaceActivationStrategy.NO_ACTION_NEEDED
    }

    override suspend fun sendLaunch(nodeId: String) {
        // TODO - tell the watch what to do!
        messageClient.sendMessage(nodeId, ANDROIDIFY_GUIDANCE_LAUNCH, byteArrayOf())
    }



    companion object {
        const val SEND_TIMEOUT_MS = 60_000L
        private val TAG = WearDeviceRepositoryImpl::class.java.simpleName
    }
}
