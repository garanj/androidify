package com.android.developers.androidify.wear.common

import kotlinx.serialization.Serializable

@Serializable
data class InitialRequest(
    val sizeInBytes: Long = 0,
    val transferId: String = "",
    val token: String = ""
)

@Serializable
data class InitialResponse(
    val proceed: Boolean
)

@Serializable
data class InstallResponse(
    val success: Boolean = false,
    val activationStrategy: Int = 0,
    val errorCode: Int = 0
)

// TODO move
sealed class WatchFaceInstallResult {
    data class Success(val activationStrategy: WatchFaceActivationStrategy) : WatchFaceInstallResult()
    data class Failure(val error: WatchFaceInstallError) : WatchFaceInstallResult()
}