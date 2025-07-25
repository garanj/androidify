package com.android.developers.androidify.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.android.developers.androidify.R
import com.android.developers.androidify.ui.theme.AndroidifyWearTheme

@Composable
fun AllDoneScreen(
    onAllDoneClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    CallToActionScreen(
        callToActionText = stringResource(R.string.all_done_prompt),
        buttonText = stringResource(R.string.all_done_button_text),
        onCallToActionClick = {
            onAllDoneClick()
            (context as Activity).finish()
        },
    )
}

@WearPreviewDevices
@Composable
fun AllDoneScreenPreview() {
    AndroidifyWearTheme {
        AllDoneScreen({})
    }
}