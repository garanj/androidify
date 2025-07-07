package com.android.developers.androidify.data

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class DropBehaviourFactory @Inject constructor(val imageGenerationRepository: ImageGenerationRepository) {

    fun createTargetCallback(
        activity: ComponentActivity,
        onImageDropped: (Uri) -> Unit,
        onDropStarted: () -> Unit = {},
        onDropEnded: () -> Unit = {},
    ) =
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                super.onStarted(event)
                onDropStarted()
            }

            override fun onEnded(event: DragAndDropEvent) {
                super.onEnded(event)
                onDropEnded()
            }

            /**
             * Dropping an image requires the app to obtain the permission to use the image being
             * dropped. This permission only lasts until the event is completed. The easiest way
             * of being able to display the image being dropped is to temporarily copy it inside
             * the app storage and use that copy for the processing.
             */
            override fun onDrop(event: DragAndDropEvent): Boolean {
                val targetEvent = event.toAndroidDragEvent()
                activity.lifecycleScope.launch {
                    val permission = activity.requestDragAndDropPermissions(targetEvent)
                    if (permission != null) {
                        try {
                            val inputUri = targetEvent.clipData.getItemAt(0).uri
                            activity.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                                val bitmap = BitmapFactory.decodeStream(inputStream)

                                bitmap?.let {
                                    val uri = imageGenerationRepository.saveImage(bitmap)
                                    onImageDropped(uri)
                                }
                            }
                        } finally {
                            permission.release()
                        }
                    }
                }
                return true
            }
        }
}