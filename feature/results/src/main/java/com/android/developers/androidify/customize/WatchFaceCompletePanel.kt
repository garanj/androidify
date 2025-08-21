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
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.android.developers.androidify.customize

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.results.R
import com.android.developers.androidify.theme.AndroidifyTheme

@Composable
fun WatchFaceCompletePanel(
    modifier: Modifier = Modifier,
    @DrawableRes iconResId: Int,
    headline: String,
    callToAction: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = iconResId),
                contentDescription = callToAction,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Column {
            Text(
                text = headline,
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.padding(top = 16.dp))
            Text(
                text = callToAction,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun LongPressPanel() {
    WatchFaceCompletePanel(
        headline = stringResource(R.string.complete_headline_continue),
        callToAction = stringResource(R.string.complete_long_press),
        iconResId = R.drawable.watch_arrow_24,
    )
}

@Composable
fun GrantPermissionsPanel() {
    WatchFaceCompletePanel(
        headline = stringResource(R.string.complete_headline_continue),
        callToAction = stringResource(R.string.complete_permissions),
        iconResId = R.drawable.watch_arrow_24,
    )
}

@Composable
fun UpdateSettingsPanel() {
    WatchFaceCompletePanel(
        headline = stringResource(R.string.complete_headline_continue),
        callToAction = stringResource(R.string.complete_settings),
        iconResId = R.drawable.watch_arrow_24,
    )
}

@Composable
fun AllDonePanel() {
    WatchFaceCompletePanel(
        headline = stringResource(R.string.complete_headline_all_done),
        callToAction = stringResource(R.string.complete_enjoy_watch_face),
        iconResId = R.drawable.watch_check_24,
    )
}

@Composable
fun ErrorPanel() {
    WatchFaceCompletePanel(
        headline = stringResource(R.string.complete_error_headline),
        callToAction = stringResource(R.string.complete_error_message),
        iconResId = R.drawable.watch_error_24,
    )
}

@Preview(showBackground = true)
@Composable
private fun LongPressPanelPreview() {
    AndroidifyTheme {
        LongPressPanel()
    }
}

@Preview(showBackground = true)
@Composable
private fun GrantPermissionsPanelPreview() {
    AndroidifyTheme {
        GrantPermissionsPanel()
    }
}

@Preview(showBackground = true)
@Composable
private fun AllDonePanelPreview() {
    AndroidifyTheme {
        AllDonePanel()
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorPanelPreview() {
    AndroidifyTheme {
        ErrorPanel()
    }
}
