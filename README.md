# Gray Matters (Grayscale Toggle)

An Android application that helps you reduce screen time by enabling grayscale mode on your device. To make it harder to switch back to color, the app introduces a "mindfulness delay"—you must hold a button for a configurable amount of time to disable grayscale.

## Features

- **Grayscale Toggle**: Easily enable or disable system-wide grayscale.
- **Delayed Unlock**: A hold-to-unlock mechanism prevents impulsive disabling of grayscale.
- **Configurable Delay**: Set your own unlock duration (in seconds), which is persisted across app restarts.
- **App Widget**: Toggle grayscale directly from your home screen (Note: The unlock challenge still applies if enabled).
- **Auto-Sync**: The app UI automatically updates its state if grayscale is changed via system settings.

## Setup Requirements

This app modifies system-wide display settings. For it to function, you must grant it the `WRITE_SECURE_SETTINGS` permission via ADB:

1. Connect your phone to your computer with USB debugging enabled.
2. Run the following command:

   ```bash
   adb shell pm grant com.tamashalasi.graymatters android.permission.WRITE_SECURE_SETTINGS
   ```
