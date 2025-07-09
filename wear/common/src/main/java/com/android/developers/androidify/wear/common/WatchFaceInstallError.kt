package com.android.developers.androidify.wear.common

enum class WatchFaceInstallError {
    NO_ERROR,
    WATCH_NOT_READY,
    SEND_SETUP_REQUEST_ERROR,
    SEND_SETUP_TIMEOUT,
    TRANSFER_ERROR,
    TRANSFER_TIMEOUT,
    DISCONNECTED,
    WATCH_FACE_INSTALL_ERROR
}