package com.snowdango.amya

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

actual object Log {

    val logger = KotlinLogging.logger {}
    val logFile = File(System.getProperty("java.io.tmpdir"), "amya.log")
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    actual fun d(message: String) {
        logger.debug { message }
        logFile.appendText("${formatter.format(ZonedDateTime.now())} [DEBUG] $message\n")
    }

    actual fun e(message: String) {
        logger.error { message }
        logFile.appendText("${formatter.format(ZonedDateTime.now())} [ERROR] $message\n")
    }

}