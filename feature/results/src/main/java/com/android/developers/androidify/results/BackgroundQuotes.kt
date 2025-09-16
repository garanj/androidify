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
package com.android.developers.androidify.results

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BackgroundQuotes(quote1: String, quote2: String?, verboseLayout: Boolean = true) {
    // Disable animation in tests
    val iterations = if (LocalInspectionMode.current) 0 else 100

    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            quote1,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = Bold),
            fontSize = 120.sp,
            modifier = Modifier
                .align(if (verboseLayout) Alignment.TopCenter else Alignment.Center)
                .basicMarquee(
                    iterations = iterations,
                    repeatDelayMillis = 0,
                    velocity = 80.dp,
                    initialDelayMillis = 500,
                ),
        )
        if (quote2 != null) {
            Text(
                quote2,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = Bold),
                fontSize = 110.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .basicMarquee(
                        iterations = iterations,
                        repeatDelayMillis = 0,
                        velocity = 60.dp,
                        initialDelayMillis = 500,
                    ),
            )
        }
    }
}

@Composable
fun BackgroundRandomQuotes(verboseLayout: Boolean = true) {
    val localInspectionMode = LocalInspectionMode.current
    val listResultCompliments = stringArrayResource(R.array.list_compliments)

    val quote1 = remember {
        if (localInspectionMode) {
            listResultCompliments.first()
        } else {
            listResultCompliments.random()
        }
    }
    val quote2 = remember {
        if (verboseLayout) {
            val listMinusOther = listResultCompliments.asList().minus(quote1)
            if (localInspectionMode) {
                listMinusOther.first()
            } else {
                listMinusOther.random()
            }
        } else {
            null
        }
    }
    BackgroundQuotes(quote1, quote2, verboseLayout)
}
