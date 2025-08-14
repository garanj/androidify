package com.android.developers.androidify.wear.common

data class ConnectedDevice(
    val nodeId: String,
    val displayName: String,
    val hasAndroidify: Boolean,
)