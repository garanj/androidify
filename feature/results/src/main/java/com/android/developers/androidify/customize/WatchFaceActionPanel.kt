package com.android.developers.androidify.results

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.theme.components.PrimaryButton


@Composable
fun InstallWatchFacePanel(
    modifier: Modifier = Modifier,
    deviceName: String,
    isSendingToWatch: Boolean,
    onButtonClick: () -> Unit = { },
) {
    WatchFaceActionPanel(
        callToAction = stringResource(R.string.send_to_watch_cta, deviceName),
        buttonText = stringResource(R.string.send_to_watch),
        isSendingToWatch = isSendingToWatch,
        onButtonClick = onButtonClick,
    )
}

@Composable
fun InstallAndroidifyPanel(modifier: Modifier = Modifier, deviceName: String) {
    val context = LocalContext.current
    WatchFaceActionPanel(
        callToAction = stringResource(R.string.install_androidify_cta, deviceName),
        isSendingToWatch = false,
        buttonText = stringResource(R.string.install_androidify),
        onButtonClick = {
            val uri = "market://details?id=${context.packageName}".toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        },
    )
}

@Composable
fun WatchFaceActionPanel(
    modifier: Modifier = Modifier,
    callToAction: String,
    buttonText: String,
    isSendingToWatch: Boolean,
    onButtonClick: () -> Unit = { },
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = callToAction,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(24.dp))
        PrimaryButton(
            onClick = onButtonClick,
            loading = isSendingToWatch,
            leadingIcon = {
                Row {
                    Icon(
                        ImageVector.vectorResource(R.drawable.watch_24),
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            },
            buttonText = buttonText,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun InstallAndroidifyPanelPreview() {
    AndroidifyTheme {
        InstallAndroidifyPanel(
            deviceName = "Pixel 3",
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun InstallWatchFacePanelPreview() {
    AndroidifyTheme {
        InstallWatchFacePanel(
            deviceName = "Pixel 3",
            isSendingToWatch = false,
        )
    }
}