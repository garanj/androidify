package com.android.developers.androidify.watchface

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.android.developers.androidify.watchface.PackPackage.Resource.Companion.fromByteArrayContents
import com.android.developers.androidify.watchface.PackPackage.Resource.Companion.fromStringContents
import com.google.android.wearable.watchface.validator.client.DwfValidatorFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.graphics.scale
import kotlin.math.abs

/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
interface WatchFaceCreator {
    fun createWatchFacePackage(botBitmap: Bitmap, watchFaceName: String = "androiddigital"): WatchFacePackage
}

@Singleton
class WatchFaceCreatorImpl @Inject constructor(
    @ApplicationContext val context: Context,
): WatchFaceCreator {
    override fun createWatchFacePackage(botBitmap: Bitmap, watchFaceName: String): WatchFacePackage {
        val watchFacePackageName = createUniqueWatchFaceName()
        val manifest = readStringAsset("$watchFaceName/AndroidManifest.xml")

        val wfPackage = PackPackage(
            combinedPemString = readStringAsset("pem_txt").trim(),
            androidManifest = replacePackageName(manifest, watchFacePackageName)
        )

        addTextFiles(watchFaceName, "raw", wfPackage)
        addTextFiles(watchFaceName, "values", wfPackage)
        addTextFiles(watchFaceName, "xml", wfPackage)
        addBinaryFiles(watchFaceName, "drawable", wfPackage)

        val bot = fromByteArrayContents(
            "drawable",
            "bot.png",
            botBitmap
                .floodFill()
                .scale(312, 312)
                .toByteArray(),
        )
        wfPackage.resources.add(bot)

        val bytes = wfPackage.compileApk()
        val watchFaceFile = File.createTempFile("watchface", ".apk")
        watchFaceFile.deleteOnExit()
        watchFaceFile.writeBytes(bytes)

        val validator = DwfValidatorFactory.create()
        val validationResult = validator.validate(watchFaceFile, context.packageName)

        if (validationResult.failures().isNotEmpty()) {
            throw IllegalStateException("Watch face validation failed: ${validationResult.failures()}")
        }

        return WatchFacePackage(
            file = watchFaceFile,
            validationToken = validationResult.validationToken(),
        )
    }

    private fun addTextFiles(watchFaceName: String, subdirectory: String, packPackage: PackPackage) {
        val assetPath = "$watchFaceName/res/$subdirectory"
        val files = context.assets.list(assetPath)
        if (files != null) {
            for (file in files) {
                val contents = readStringAsset("$assetPath/$file").trim()
                val resource = fromStringContents(subdirectory, file, contents)
                packPackage.resources.add(resource)
            }
        }
    }

    private fun addBinaryFiles(watchFaceName: String, subdirectory: String, packPackage: PackPackage) {
        val assetPath = "$watchFaceName/res/$subdirectory"
        val files = context.assets.list(assetPath)
        if (files != null) {
            for (file in files) {
                val contents = readBinaryAsset("$assetPath/$file")
                val resource = fromByteArrayContents(subdirectory, file, contents)
                packPackage.resources.add(resource)
            }
        }
    }

    private fun readStringAsset(fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }

    private fun readBinaryAsset(fileName: String): ByteArray {
        return context.assets.open(fileName).readBytes()
    }

    private fun replacePackageName(text: String, newPackageName: String): String {
        val regex = Regex("""package="([^"]*)"""")
        return regex.replace(text) {
            "package=\"$newPackageName\""
        }
    }

    private fun createUniqueWatchFaceName() =
        context.packageName + ".watchfacepush.bot" + UUID.randomUUID().toString()
            .replace("-", "").take(12)

    private fun Bitmap.toByteArray(format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 80): ByteArray {
        val stream = ByteArrayOutputStream()
        this.compress(format, quality, stream)
        return stream.toByteArray()
    }

    // TODO: Remove this when the bitmap has a transparent background anyway
    fun Bitmap.floodFill(
        startX: Int = 0,
        startY: Int = 0,
        baseColor: Int = Color.WHITE,
        tolerance: Int = 10,
        replacementColor: Int = Color.TRANSPARENT
    ): Bitmap {
        val width = this.width
        val height = this.height
        val pixels = IntArray(width * height)
        this.getPixels(pixels, 0, width, 0, 0, width, height)

        val startPixel = pixels[startY * width + startX]

        if (!isColorWithinTolerance(startPixel, baseColor, tolerance)) {
            return this
        }

        // Use ArrayDeque for a more efficient queue implementation
        val queue = ArrayDeque<Pair<Int, Int>>()
        val visited = BooleanArray(width * height)

        queue.add(Pair(startX, startY))
        visited[startY * width + startX] = true

        while (queue.isNotEmpty()) {
            val (x, y) = queue.removeFirst()
            val currentIndex = y * width + x

            pixels[currentIndex] = replacementColor

            for ((dx, dy) in listOf(Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1))) {
                val nx = x + dx
                val ny = y + dy
                val neighborIndex = ny * width + nx

                if (nx in 0 until width && ny in 0 until height && !visited[neighborIndex]) {
                    visited[neighborIndex] = true
                    val neighborPixel = pixels[neighborIndex]

                    if (isColorWithinTolerance(neighborPixel, baseColor, tolerance)) {
                        queue.addLast(Pair(nx, ny))
                    }
                }
            }
        }

        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun isColorWithinTolerance(pixelColor: Int, baseColor: Int, tolerance: Int): Boolean {
        // Skip already transparent pixels
        if (Color.alpha(pixelColor) == 0) return false

        val baseRed = Color.red(baseColor)
        val baseGreen = Color.green(baseColor)
        val baseBlue = Color.blue(baseColor)

        val pixelRed = Color.red(pixelColor)
        val pixelGreen = Color.green(pixelColor)
        val pixelBlue = Color.blue(pixelColor)

        return abs(pixelRed - baseRed) <= tolerance &&
                abs(pixelGreen - baseGreen) <= tolerance &&
                abs(pixelBlue - baseBlue) <= tolerance
    }
}