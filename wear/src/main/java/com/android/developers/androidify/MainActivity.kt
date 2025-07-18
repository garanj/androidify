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
package com.android.developers.androidify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.wear.ambient.AmbientLifecycleObserver
import com.android.developers.androidify.ui.WatchFaceOnboardingScreen
import com.android.developers.androidify.ui.theme.AndroidifyWearTheme

class MainActivity : ComponentActivity() {
    val ambientCallback = object : AmbientLifecycleObserver.AmbientLifecycleCallback {
        override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) {  }

        override fun onExitAmbient() { }

        override fun onUpdateAmbient() { }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(AmbientLifecycleObserver(this, ambientCallback))

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            AndroidifyWearTheme {
                WatchFaceOnboardingScreen()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(AmbientLifecycleObserver(this, ambientCallback))
    }
}
