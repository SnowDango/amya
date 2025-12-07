package com.snowdango.amya.platform

import ca.gosyer.appdirs.AppDirs
import com.snowdango.amya.BuildKonfig

actual class Platform {
    actual val javaVersion: String = System.getProperty("java.version")
    actual val osName: String = System.getProperty("os.name")
    actual val osVersion: String = System.getProperty("os.version")
    actual val platformType = when (BuildKonfig.osName) {
        "Windows" -> PlatformType.WINDOWS
        "Mac OS X" -> PlatformType.MACOS
        "Linux" -> PlatformType.LINUX
        else -> throw IllegalStateException("Unknown OS: ${BuildKonfig.osName}")
    }
}

actual fun getPlatform() = Platform()

fun getAppDirs() = AppDirs {
    appName = "Amya"
    appAuthor = "SnowDango"
}
