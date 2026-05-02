package com.tamashalasi.graymatters

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.tamashalasi.graymatters.ui.theme.GrayMattersTheme
import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GrayMattersTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    GrayscaleController() // This calls the UI we built
                }
            }
        }
    }
}

@Composable
fun GrayscaleController() {
    val context = LocalContext.current
    var isGrayscaleEnabled by remember { mutableStateOf(checkGrayscaleStatus(context)) }
    var unlockTimeInput by remember { mutableStateOf("5") } // Default 5 seconds
    var holdProgress by remember { mutableFloatStateOf(0f) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (!isGrayscaleEnabled) {
            // UI FOR DISABLED STATE
            Text("Grayscale is OFF", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = unlockTimeInput,
                onValueChange = { 
                    if (it.all { char -> char.isDigit() }) {
                        unlockTimeInput = it
                        errorMessage = null
                    }
                },
                label = { Text("Unlock Time (seconds)") },
                modifier = Modifier.fillMaxWidth(),
                isError = unlockTimeInput.isEmpty()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (unlockTimeInput.isEmpty()) {
                        errorMessage = "Please enter a valid time."
                        return@Button
                    }
                    if (setGrayscale(context, true)) {
                        isGrayscaleEnabled = true
                        errorMessage = null
                    } else {
                        errorMessage = "Failed to enable grayscale. Run: adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enable Grayscale")
            }
        } else {
            // UI FOR ENABLED STATE (THE UNLOCK CHALLENGE)
            Text("Grayscale is ON", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))

            LinearProgressIndicator(
                progress = { holdProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            val requiredTimeMs = remember(unlockTimeInput) { (unlockTimeInput.toIntOrNull() ?: 5) * 1000L }

            Surface(
                tonalElevation = 4.dp,
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .pointerInput(requiredTimeMs) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitFirstDown()
                                val startTime = System.currentTimeMillis()
                                var isHolding = true

                                val job = scope.launch {
                                    while (isHolding) {
                                        val elapsed = System.currentTimeMillis() - startTime
                                        holdProgress = (elapsed.toFloat() / requiredTimeMs).coerceAtMost(1f)

                                        if (elapsed >= requiredTimeMs) {
                                            if (setGrayscale(context, false)) {
                                                isGrayscaleEnabled = false
                                                errorMessage = null
                                            } else {
                                                errorMessage = "Failed to disable grayscale. Run: adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS"
                                            }
                                            holdProgress = 0f
                                            isHolding = false
                                            break
                                        }
                                        delay(16) // ~60fps update
                                    }
                                }

                                try {
                                    waitForUpOrCancellation()
                                } finally {
                                    isHolding = false
                                    job.cancel()
                                    holdProgress = 0f
                                }
                            }
                        }
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "Hold to Disable (${unlockTimeInput}s)",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

// Logic to check system settings
fun checkGrayscaleStatus(context: Context): Boolean {
    return Settings.Secure.getInt(
        context.contentResolver,
        "accessibility_display_daltonizer_enabled", 0
    ) == 1
}

// Logic to write system settings
fun setGrayscale(context: Context, enable: Boolean): Boolean {
    return try {
        val mode = if (enable) 0 else -1 // 0 is grayscale, -1 is disabled
        val success1 = Settings.Secure.putInt(context.contentResolver, "accessibility_display_daltonizer_enabled", if (enable) 1 else 0)
        val success2 = Settings.Secure.putInt(context.contentResolver, "accessibility_display_daltonizer", mode)
        success1 && success2
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

@Preview(showBackground = true)
@Composable
fun GrayscaleControllerPreview() {
    GrayMattersTheme {
        GrayscaleController()
    }
}
