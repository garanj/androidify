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
package com.android.developers.androidify.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.android.developers.androidify.R
import com.android.developers.androidify.ui.theme.AndroidifyWearTheme
import com.android.developers.androidify.ui.theme.Blue
import com.android.developers.androidify.ui.theme.LimeGreen
import com.android.developers.androidify.ui.theme.Primary80
import com.android.developers.androidify.ui.theme.Primary90
import com.google.android.horologist.compose.layout.ColumnItemType
import com.google.android.horologist.compose.layout.rememberResponsiveColumnPadding
import kotlin.math.floor

@Composable
fun TransmissionScreen(modifier: Modifier = Modifier) {
    KeepScreenOn()
    val listState = rememberTransformingLazyColumnState()
    ScreenScaffold(
        scrollState = listState,
        // Use Horologist for now to get correct top and bottom padding in list.
        contentPadding = rememberResponsiveColumnPadding(
            first = ColumnItemType.IconButton,
            last = ColumnItemType.Button,
        ),
    ) { contentPadding ->
        TransformingLazyColumn(
            state = listState,
            contentPadding = contentPadding,
        ) {
            item {
                Image(
                    modifier = Modifier.fillMaxWidth(0.3f),
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = stringResource(R.string.logo_description),
                )
            }
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }
            item {
                FourColorProgressIndicator()
            }
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }
            item {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = stringResource(R.string.receiving),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = view.context.findActivity()?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun FourColorProgressIndicator() {
    val colors = listOf(
        LimeGreen,
        Primary80,
        Primary90,
        Blue,
    )

    val infiniteTransition = rememberInfiniteTransition(label = "transition")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = colors.size.toFloat(), // Animate from 0 to 4
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
        ),
        label = "progress",
    )

    val colorIndex = floor(progress).toInt()
    val progressFraction = progress - colorIndex

    val currentColor = colors[colorIndex % colors.size]
    val nextColor = colors[(colorIndex + 1) % colors.size]
    val animatedColor = lerp(start = currentColor, stop = nextColor, fraction = progressFraction)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        CircularProgressIndicator(indicatorColor = animatedColor)
    }
}

@WearPreviewDevices
@Composable
fun TransmissionScreenPreview() {
    AndroidifyWearTheme {
        TransmissionScreen()
    }
}
