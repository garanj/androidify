package com.android.developers.androidify.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.android.developers.androidify.R
import com.android.developers.androidify.ui.theme.AndroidifyWearTheme

@Composable
fun PermissionsPromptScreen(
    modifier: Modifier = Modifier,
    launchPermissionRequest: () -> Unit,
) {
    CallToActionScreen(
        callToActionText = stringResource(R.string.permissions_prompt),
        buttonText = stringResource(R.string.permissions_button_text),
        onCallToActionClick = launchPermissionRequest,
    )
}

@WearPreviewDevices
@Composable
fun PermissionsPromptScreenPreview() {
    AndroidifyWearTheme {
        PermissionsPromptScreen(
            launchPermissionRequest = {},
        )
    }
}