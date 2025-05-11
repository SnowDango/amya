package com.snowdango.amya.platform

import ca.gosyer.appdirs.AppDirs

actual class Platform {
    actual val javaVersion: String = System.getProperty("java.version")
    actual val osName: String = System.getProperty("os.name")
    actual val osVersion: String = System.getProperty("os.version")
    actual val platformType = when {
        "Mac" in osName -> PlatformType.MACOS
        "Windows" in osName -> PlatformType.WINDOWS
        else -> {
            Log.d("osName: $osName")
            PlatformType.LINUX
        }
    }
}

actual fun getPlatform() = Platform()

fun getAppDirs(): AppDirs {
    return AppDirs(
        appName = "Amya",
        appAuthor = "SnowDango",
    )
}