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
package com.android.developers.androidify.data

import android.content.Context
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class ConnectedDevice(
    val nodeId: String,
    val displayName: String,
    val hasAndroidify: Boolean,
)

interface WearDeviceRepository {
    val connectedDevice: Flow<ConnectedDevice?>
}

@Singleton
class WearDeviceRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
) : WearDeviceRepository {
    private val nodeClient: NodeClient by lazy { Wearable.getNodeClient(context) }
    private val capabilityClient: CapabilityClient by lazy { Wearable.getCapabilityClient(context) }

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

    companion object {
        const val ANDROIDIFY_INSTALLED = "androidify_installed"
    }
}
