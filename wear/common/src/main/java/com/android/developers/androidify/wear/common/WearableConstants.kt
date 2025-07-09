package com.android.developers.androidify.wear.common

object WearableConstants {
    // DataClient
    const val ANDROIDIFY_DATA_PATH_TEMPLATE = "/androidify_bot/%s"
    const val ANDROIDIFY_APK_KEY = "androidify_apk"
    const val ANDROIDIFY_VALIDATION_TOKEN_KEY = "androidify_validation_token"

    // MessageClient
    const val ANDROIDIFY_GUIDANCE_LAUNCH = "/androidify_bot/launch"

    const val ANDROIDIFY_INITIATE_TRANSFER_PATH = "/initiate_transfer"
    const val ANDROIDIFY_FINALIZE_TRANSFER_TEMPLATE = "/finalize_transfer/%s"
    const val ANDROIDIFY_CANCEL_PATH_TEMPLATE = "/cancel_transfer/%s"
    const val ANDROIDIFY_CANCEL_PATH = "/cancel_transfer/"

    // CapabilityClient
    const val ANDROIDIFY_INSTALLED = "androidify"
    const val ANDROIDIFY_TRANSFER_PATH_TEMPLATE = "/transfer_apk/%s"

    const val SETUP_TIMEOUT_MS = 30_000L
    const val TRANSFER_TIMEOUT_MS = 30_000L
}