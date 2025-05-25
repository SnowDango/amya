package com.snowdango.amya.platform

expect object AutoLaunchExtension {

    suspend fun isEnabled(): Boolean

    suspend fun enable()

    suspend fun disable()
}
