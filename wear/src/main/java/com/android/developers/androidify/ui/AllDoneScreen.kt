package com.android.developers.androidify.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.android.developers.androidify.R
import com.android.developers.androidify.ui.theme.AndroidifyWearTheme

@Composable
fun AllDoneScreen(
    onAllDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val finalAction = {
        onAllDone()
        (context as Activity).finishAndRemoveTask()
    }

    DisposableEffect(Unit) {
        onDispose {
            finalAction()
        }
    }

    CallToActionScreen(
        callToActionText = stringResource(R.string.all_done_prompt),
        buttonText = stringResource(R.string.all_done_button_text),
        onCallToActionClick = finalAction,
    )
}

@WearPreviewDevices
@Composable
fun AllDoneScreenPreview() {
    AndroidifyWearTheme {
        AllDoneScreen({})
    }
}