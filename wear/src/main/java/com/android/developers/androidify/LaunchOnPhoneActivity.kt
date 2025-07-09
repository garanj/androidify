package com.android.developers.androidify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import androidx.wear.remote.interactions.RemoteActivityHelper
import androidx.wear.widget.ConfirmationOverlay
import androidx.wear.widget.ConfirmationOverlay.OPEN_ON_PHONE_ANIMATION
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.runBlocking

/**
 * A helper activity that launches the phone Androidify app. This Activity is only started from the
 * default watch face and does not form part of the rest of the Wear OS app experience.
 */
class LaunchOnPhoneActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(Intent.ACTION_VIEW, "androidify://launch".toUri())
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        val helper = RemoteActivityHelper(this)
        val message: CharSequence = getString(R.string.continue_on_phone)

        ConfirmationOverlay()
            .setType(OPEN_ON_PHONE_ANIMATION)
            .setDuration(5000)
            .setMessage(message)
            .setOnAnimationFinishedListener {
                onAnimationFinished()
            }
            .showOn(this)

        runBlocking {
            try {
                helper.startRemoteActivity(intent).await()
            } catch (e: RemoteActivityHelper.RemoteIntentException) {
                Log.e("LaunchOnPhoneActivity", "Error launching on phone", e)
            }
        }
    }

    fun onAnimationFinished() {
        finish()
    }
}