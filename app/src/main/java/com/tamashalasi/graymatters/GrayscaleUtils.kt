package com.tamashalasi.graymatters

import android.content.Context
import android.provider.Settings
import androidx.core.content.edit

object GrayscaleUtils {
    private const val PREFS_NAME = "grayscale_settings"
    private const val KEY_UNLOCK_TIMEOUT = "unlock_timeout"

    fun getUnlockTimeout(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_UNLOCK_TIMEOUT, "5") ?: "5"
    }

    fun setUnlockTimeout(context: Context, timeout: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_UNLOCK_TIMEOUT, timeout) }
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
