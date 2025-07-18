package com.android.developers.androidify.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.android.developers.androidify.R
import com.android.developers.androidify.ui.theme.AndroidifyWearTheme

/**
 * If the user has already denied the permission to set the watch face, then this screen is shown.
 */
@Composable
fun OpenSettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    CallToActionScreen(
        callToActionText = stringResource(R.string.open_settings_prompt),
        buttonText = stringResource(R.string.open_settings_button_text),
        onCallToActionClick = {
            val intent =
                Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", context.packageName, null)
                }
            (context as Activity).startActivity(intent)
        },
    )
}

@WearPreviewDevices
@Composable
fun OpenSettingsPreview() {
    AndroidifyWearTheme {
        OpenSettingsScreen()
    }
}