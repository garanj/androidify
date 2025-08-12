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
package com.android.developers.androidify.ondevice

import android.graphics.Bitmap
import android.print.PrintJobInfo.STATE_COMPLETED
import android.util.Log
import com.google.android.gms.common.moduleinstall.InstallStatusListener
import com.google.android.gms.common.moduleinstall.ModuleInstallClient
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate.InstallState.STATE_CANCELED
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate.InstallState.STATE_FAILED
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface LocalSegmentationDataSource {
    suspend fun removeBackground(bitmap: Bitmap): Bitmap
}

class LocalSegmentationDataSourceImpl @Inject constructor(
    private val moduleInstallClient: ModuleInstallClient
) : LocalSegmentationDataSource {
    private val segmenter by lazy {
        val options = SubjectSegmenterOptions.Builder()
            .enableForegroundBitmap()
            .build()
        SubjectSegmentation.getClient(options)
    }

    private suspend fun isSubjectSegmentationModuleInstalled(): Boolean {
        val areModulesAvailable =
            suspendCoroutine { continuation ->
                moduleInstallClient.areModulesAvailable(segmenter)
                    .addOnSuccessListener {
                        continuation.resume(it.areModulesAvailable())
                    }
                    .addOnFailureListener {
                        continuation.resumeWithException(it)
                    }
            }
        return areModulesAvailable
    }

    private suspend fun installSubjectSegmentationModule(): Boolean {
        val result = suspendCoroutine { continuation ->
            val listener = InstallStatusListener { update ->
                Log.d("LocalSegmentationDataSource", "Download progress: ${update.installState}")

                if (update.installState == STATE_COMPLETED) {
                    continuation.resume(true)
                } else if (update.installState == STATE_FAILED || update.installState == STATE_CANCELED) {
                    continuation.resumeWithException(
                        Exception("Module download failed or was canceled. State: ${update.installState}")
                    )
                } else {
                    Log.d("LocalSegmentationDataSource", "State update: ${update.installState}")
                }
            }
            val moduleInstallRequest = ModuleInstallRequest.newBuilder()
                .addApi(segmenter)
                .setListener(listener)
                .build()

            moduleInstallClient
                .installModules(moduleInstallRequest)
                .addOnFailureListener {
                    Log.e("LocalSegmentationDataSource", "Failed to download module", it)
                }
                .addOnCompleteListener {
                    Log.d("LocalSegmentationDataSource", "Successfully triggered download - await download progress updates")
                }

        }
        return result
    }

    override suspend fun removeBackground(bitmap: Bitmap): Bitmap {
        val areModulesAvailable = isSubjectSegmentationModuleInstalled()

        if (!areModulesAvailable) {
            Log.d("LocalSegmentationDataSource", "Modules not available - downloading")
            val result = installSubjectSegmentationModule()
            if (!result) {
                throw Exception("Failed to download module")
            }
        } else {
            Log.d("LocalSegmentationDataSource", "Modules available")
        }
        val image = InputImage.fromBitmap(bitmap, 0)
        return suspendCoroutine { continuation ->
            segmenter.process(image)
                .addOnSuccessListener { result ->
                    if (result.foregroundBitmap != null) {
                        continuation.resume(result.foregroundBitmap!!)
                    } else {
                        continuation.resumeWithException(Exception("Subject not found"))
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("LocalSegmentationDataSource", "Exception while executing background removal", e)
                    continuation.resumeWithException(e)
                }
        }
    }
}
