package com.snowdango.amya.platform

import io.github.vinceglb.autolaunch.AutoLaunch

actual object AutoLaunchExtension {

    private val autoLaunch = AutoLaunch(appPackageName = "Amya")

    actual suspend fun isEnabled(): Boolean {
        return autoLaunch.isEnabled()
    }

    actual suspend fun enable() {
        autoLaunch.enable()
    }

    actual suspend fun disable() {
        autoLaunch.disable()
    }
}
