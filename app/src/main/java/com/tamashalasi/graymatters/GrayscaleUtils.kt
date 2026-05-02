package com.tamashalasi.graymatters

import android.content.Context
import android.provider.Settings

object GrayscaleUtils {
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
            val success1 = Settings.Secure.putInt(
                context.contentResolver,
                "accessibility_display_daltonizer_enabled",
                if (enable) 1 else 0
            )
            val success2 = Settings.Secure.putInt(
                context.contentResolver,
                "accessibility_display_daltonizer",
                mode
            )
            success1 && success2
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
