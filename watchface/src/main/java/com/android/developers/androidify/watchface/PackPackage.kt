package com.android.developers.androidify.watchface

import android.util.Base64
import java.nio.charset.StandardCharsets

data class PackPackage(
    val androidManifest: String,
    val combinedPemString: String
) {
    var resources: MutableList<Resource> = mutableListOf()

    data class Resource(
        val subdirectory: String,
        val name: String,
        val contentsBase64: String
    ) {
        companion object {
            fun fromBase64Contents(
                subdirectory: String,
                name: String,
                contentsBase64: String
            ) = Resource(subdirectory, name, contentsBase64)

            fun fromByteArrayContents(
                subdirectory: String,
                name: String,
                contentsBytes: ByteArray
            ): Resource {
                val encodedContents = Base64.encodeToString(contentsBytes, Base64.NO_WRAP)
                return fromBase64Contents(subdirectory, name, encodedContents)
            }

            fun fromStringContents(
                subdirectory: String,
                name: String,
                contentsString: String
            ): Resource {
                val bytes = contentsString.toByteArray(StandardCharsets.UTF_8)
                return fromByteArrayContents(subdirectory, name, bytes)
            }
        }
    }

    fun compileApk(): ByteArray = compilePackage(apk = true)

    private fun compilePackage(apk: Boolean): ByteArray {
        val resultBase64 = nativeCompilePackage(
            androidManifest,
            resources.toTypedArray(),
            combinedPemString,
            apk
        )
        return Base64.decode(resultBase64, Base64.DEFAULT)
    }

    companion object {
        init {
            System.loadLibrary("pack_java")
        }

        @JvmStatic
        private external fun nativeCompilePackage(
            androidManifest: String,
            resources: Array<Resource>,
            combinedPemString: String,
            apk: Boolean
        ): String
    }
}