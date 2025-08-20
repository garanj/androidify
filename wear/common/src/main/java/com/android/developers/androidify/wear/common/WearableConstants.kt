package com.android.developers.androidify.wear.common

object WearableConstants {
    const val ANDROIDIFY_INITIATE_TRANSFER_PATH = "/initiate_transfer"
    const val ANDROIDIFY_FINALIZE_TRANSFER_TEMPLATE = "/finalize_transfer/%s"

    const val ANDROIDIFY_INSTALLED = "androidify"
    const val ANDROIDIFY_TRANSFER_PATH_TEMPLATE = "/transfer_apk/%s"

    const val SETUP_TIMEOUT_MS = 60_000L
    const val TRANSFER_TIMEOUT_MS = 60_000L
}