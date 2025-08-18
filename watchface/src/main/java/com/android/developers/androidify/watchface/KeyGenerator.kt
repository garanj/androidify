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
package com.android.developers.androidify.watchface

import android.util.Base64
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.StringWriter
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.util.Calendar
import java.util.Date

/**
 * Generates a self-signed X.509 certificate and its private key. This is used to sign the APK.
 *
 * This is NOT the approach that should be taken in a production app.
 *
 * @return A String containing the certificate and private key in PEM format.
 */
fun generateSelfSignedCertificateAndKey(): String {
    val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
    keyPairGenerator.initialize(2048) // Key size
    val keyPair = keyPairGenerator.generateKeyPair()

    val now = Date()
    val calendar = Calendar.getInstance()
    calendar.time = now
    calendar.add(Calendar.YEAR, 10)
    val expiryDate = calendar.time

    val serialNumber = BigInteger(64, SecureRandom())
    val name = "C=US,ST=Test,L=Test,O=Test,OU=Test,CN=test.example.com"

    val x500NameFromString = X500Name(BCStyle.INSTANCE, name)

    val certificateBuilder = JcaX509v3CertificateBuilder(
        x500NameFromString,
        serialNumber,
        now,
        expiryDate,
        x500NameFromString,
        keyPair.public,
    )

    val contentSigner = JcaContentSignerBuilder("SHA256WithRSA")
        .build(keyPair.private)

    val certificateHolder = certificateBuilder.build(contentSigner)
    val certificate = JcaX509CertificateConverter()
        .getCertificate(certificateHolder)

    val certPem = encodeToPem("CERTIFICATE", certificate.encoded)
    val keyPem = encodeToPem("PRIVATE KEY", keyPair.private.encoded)

    return certPem + keyPem
}

/**
 * Helper function to encode raw byte data into PEM format.
 */
private fun encodeToPem(type: String, data: ByteArray): String {
    val stringWriter = StringWriter()
    stringWriter.write("-----BEGIN $type-----\n")
    stringWriter.write(
        Base64
            .encodeToString(data, Base64.NO_WRAP)
            .chunked(64).joinToString("\n"),
    )
    stringWriter.write("\n-----END $type-----\n")
    return stringWriter.toString()
}
