package com.android.developers.androidify.ui

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.android.developers.androidify.LaunchOnPhoneActivity
import com.android.developers.androidify.R
import com.android.developers.androidify.ui.theme.AndroidifyWearTheme

@Composable
fun WelcomeToAndroidifyScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    CallToActionScreen(
        callToActionText = stringResource(R.string.welcome),
        buttonText = stringResource(R.string.continue_on_phone),
        onCallToActionClick = {
            val intent = Intent(context, LaunchOnPhoneActivity::class.java)
            (context as Activity).startActivity(intent)
        },
    )
}

@WearPreviewDevices
@Composable
fun WelcomeToAndroidifyScreenPreview() {
    AndroidifyWearTheme {
        WelcomeToAndroidifyScreen()
    }
}