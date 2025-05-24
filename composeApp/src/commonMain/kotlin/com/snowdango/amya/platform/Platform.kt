package com.snowdango.amya.platform

expect class Platform {
    val javaVersion: String
    val osName: String
    val osVersion: String
    val platformType: PlatformType
}

enum class PlatformType {
    WINDOWS,
    MACOS,
    LINUX,
}

expect fun getPlatform(): Platform
