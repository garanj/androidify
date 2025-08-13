package com.android.developers.androidify.watchface

import java.io.File

data class WatchFacePackage(
    val file: File,
    val validationToken: String
)