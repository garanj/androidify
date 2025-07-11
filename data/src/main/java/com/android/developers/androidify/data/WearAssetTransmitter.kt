@file:OptIn(ExperimentalSerializationApi::class)

package com.android.developers.androidify.data

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.android.developers.androidify.wear.common.InitialRequest
import com.android.developers.androidify.wear.common.InitialResponse
import com.android.developers.androidify.wear.common.WatchFaceInstallError
import com.android.developers.androidify.wear.common.WatchFaceInstallationStatus
import com.android.developers.androidify.wear.common.WearableConstants
import com.android.developers.androidify.wear.common.WearableConstants.ANDROIDIFY_CANCELLED_BY_USER
import com.android.developers.androidify.wear.common.WearableConstants.SETUP_TIMEOUT_MS
import com.android.developers.androidify.wear.common.WearableConstants.TRANSFER_TIMEOUT_MS
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface WearAssetTransmitter {
    suspend fun receiveInstallUpdate(): Flow<WatchFaceInstallationStatus.Complete>

    suspend fun doTransfer(
        nodeId: String,
        apkFile: File,
        validationToken: String,
    ): WatchFaceInstallError
}

@Singleton
class WearAssetTransmitterImpl @Inject constructor(
    @ApplicationContext val context: Context,
) : WearAssetTransmitter {
    private val channelClient: ChannelClient by lazy { Wearable.getChannelClient(context) }
    private val messageClient: MessageClient by lazy { Wearable.getMessageClient(context) }

    private var transferId = UUID.randomUUID().toString().take(8)

    /** Sends the watch face to the watch. The approach taken is as follows:
     *
     * 1. Setup        - the phone sends a message to the watch asking for the go-ahead to send the
     *                   watch face. The watch only accepts one ongoing watch face transmission at a
     *                   time. As part of the setup, the phone sends a unique ID which is used in
     *                   the subsequent exchanges, as well as the validation token.
     * 2. Transfer     - the phone sends the watch face
     * 3. Confirmation - The watch sends a confirmation message back to the phone, indicating
     *                   success as well as the activation strategy to use. If there was an error
     *                   then the details of the error are sent.
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun receiveInstallUpdate() = callbackFlow<WatchFaceInstallationStatus.Complete> {

        val listener = MessageClient.OnMessageReceivedListener { event ->
            if (event.path.contains(transferId)) {
                val response =
                    ProtoBuf.decodeFromByteArray<WatchFaceInstallationStatus.Complete>(event.data)
                if (response.transferId == transferId) {
                    trySend(response)
                }
            }
        }

        messageClient.addListener(listener).await()

        awaitClose {
            messageClient.removeListener(listener).addOnSuccessListener { }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun doTransfer(
        nodeId: String,
        apkFile: File,
        validationToken: String,
    ): WatchFaceInstallError {
        // TODO: Replace this with the actual generated watch face
        Log.i(TAG, "Doing transfer")
        val watchFaceBytes = context.assets.open("example.apk").readAllBytes()
        val watchFaceFile = File.createTempFile("example", ".apk")
        watchFaceFile.writeBytes(watchFaceBytes)
        Log.i(TAG, "Temp file created")
        // End temporary block

        // Hard-coded token for temporary use
        val token = "E+LO9PKpm/uaJ7gZwSDO3CmnL16wDJrhpcX49A2Gdm8=:MS4wLjA="

        var readyToReceive = false
        val maybeTransferId = UUID.randomUUID().toString().take(8)
        try {
            readyToReceive = assetTransferSetup(nodeId, maybeTransferId, token, watchFaceFile)
        } catch (e: TimeoutCancellationException) {
            return WatchFaceInstallError.SEND_SETUP_TIMEOUT
        } catch (e: Exception) {
            Log.e(TAG, "Error sending request.", e)
            return WatchFaceInstallError.SEND_SETUP_REQUEST_ERROR
        }

        if (!readyToReceive) {
            return WatchFaceInstallError.WATCH_NOT_READY
        }
        transferId = maybeTransferId

        try {
            assetTransfer(nodeId, watchFaceFile)
        } catch (e: TimeoutCancellationException) {
            return WatchFaceInstallError.TRANSFER_TIMEOUT
        } catch (e: Exception) {
            Log.e(TAG, "Error transferring watch face", e)
            return WatchFaceInstallError.TRANSFER_ERROR
        }
        return WatchFaceInstallError.NO_ERROR
    }

    private suspend fun assetTransferSetup(
        nodeId: String,
        transferId: String,
        token: String,
        file: File,
    ): Boolean {
        val initialRequest = InitialRequest(transferId = transferId, token = token, sizeInBytes = file.length())
        val requestBytes = ProtoBuf.encodeToByteArray(initialRequest)

        val response = withContext(Dispatchers.IO) {
            withTimeout(SETUP_TIMEOUT_MS) {
                val responseBytes = messageClient.sendRequest(
                    nodeId,
                    WearableConstants.ANDROIDIFY_INITIATE_TRANSFER_PATH,
                    requestBytes,
                ).await()
                ProtoBuf.decodeFromByteArray<InitialResponse>(responseBytes)
            }
        }
        return response.proceed
    }

    private suspend fun assetTransfer(nodeId: String, file: File) {
        withContext(Dispatchers.IO) {
            withTimeout(TRANSFER_TIMEOUT_MS) {
                val channelPath = WearableConstants.ANDROIDIFY_TRANSFER_PATH_TEMPLATE.format(transferId)
                val channel = channelClient.openChannel(nodeId, channelPath).await()
                channelClient.sendFile(channel, Uri.fromFile(file)).await()
            }
        }
    }

    companion object {
        private val TAG = WearAssetTransmitterImpl::class.java.simpleName
    }
}