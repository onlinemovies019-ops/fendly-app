# Fendly Device Runner

The Android 10 Redmi Note 7 Pro resets ADB-over-Wi-Fi when the phone reboots.
This is an Android limitation: if both the phone and Mac were switched off, the
phone must be connected to the Mac by USB once after startup to enable Wi-Fi
ADB again.

## Reconnect after a reboot

1. Connect the Redmi and Mac to the same Wi-Fi network.
2. Connect the phone to the Mac with USB, unlock it, and approve any ADB prompt.
3. In VS Code's **Android Screen Mirror / VS Scrcpy** panel, click
   **Reconnect Fendly over Wi-Fi**.
4. Once VS Code confirms the Wi-Fi connection, unplug USB if desired.

The action reads the phone's current Wi-Fi address, enables ADB on port 5555,
and remembers the address for later Mac restarts. If only the Mac restarts while
the phone remains on, the same action tries the saved address without requiring
USB.

Confirm the connection in a terminal with `adb devices -l`; the Redmi should
appear as an IP address ending in `:5555` with state `device`. Use **Build,
Install & Run Fendly** in the same panel to launch the app.
