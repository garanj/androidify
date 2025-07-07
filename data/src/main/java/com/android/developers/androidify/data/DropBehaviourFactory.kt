package com.android.developers.androidify.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import javax.inject.Inject
import java.io.File
import java.io.FileOutputStream

class DropBehaviourFactory @Inject constructor() {

    fun createTargetCallback(activity: ComponentActivity,
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

            override fun onDrop(event: DragAndDropEvent): Boolean {
                val targetEvent = event.toAndroidDragEvent()
                val permission = activity.requestDragAndDropPermissions(targetEvent)
                if (permission != null) {
                    try {
                        val inputUri = targetEvent.clipData.getItemAt(0).uri
                        processImage(inputUri)
                    } catch (s: SecurityException) {
                        s.printStackTrace()
                    } finally {
                        permission.release()
                    }
                    return true
                } else {
                    return false
                }
            }

            private fun processImage(input: Uri) {
                activity.contentResolver.openInputStream(input)?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    if (bitmap != null) {
                        val outputFileName = "dropped_image_${System.currentTimeMillis()}.jpg"
                        val outputFile = File(
                            activity.filesDir,
                            outputFileName,
                        )

                        FileOutputStream(outputFile).use { fos ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                        }
                        onImageDropped(Uri.fromFile(outputFile))
                    } else {
                        Log.e("DragDrop", "Failed to decode bitmap from URI: $input")
                    }
                }
            }
        }
}