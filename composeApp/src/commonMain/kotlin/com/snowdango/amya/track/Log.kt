package com.snowdango.amya.track

import com.diamondedge.logging.KmLog
import com.diamondedge.logging.KmLogging
import com.diamondedge.logging.Logger
import com.diamondedge.logging.logging
import com.snowdango.amya.BuildKonfig
import java.io.File
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object Log {

    fun getEventLogger(): Logger {
        return EventLogger()
    }

    fun logger(tag: String? = null): KmLog {
        if (tag != null) {
            return logging(tag = tag)
        }
        val (_, className) = KmLogging.createTag("Log")
        return logging(tag = className)
    }

    fun d(msg: String, tag: String? = null) {
        logger(tag).d { msg }
    }

    fun i(msg: String, tag: String? = null) {
        logger(tag).i { msg }
    }

    fun w(msg: String, tag: String? = null) {
        logger(tag).w { msg }
    }

    fun e(msg: String, tag: String? = null) {
        logger(tag).e { msg }
    }

    fun v(msg: String, tag: String? = null) {
        logger(tag).v { msg }
    }

    private class EventLogger : Logger {

        val logFile = File(System.getProperty("java.io.tmpdir"), "amya.log")
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

        override fun verbose(tag: String, msg: String) {
            val log = "${formatter.format(ZonedDateTime.now())} [VERBOSE] $tag $msg\n"
            printLogToFile(log)
            println(log)
        }

        override fun debug(tag: String, msg: String) {
            val log = "${formatter.format(ZonedDateTime.now())} [DEBUG] $tag $msg\n"
            printLogToFile(log)
            println(log)
        }

        override fun info(tag: String, msg: String) {
            val log = "${formatter.format(ZonedDateTime.now())} [INFO] $tag $msg\n"
            printLogToFile(log)
            println(log)
        }

        override fun warn(tag: String, msg: String, t: Throwable?) {
            val log = "${formatter.format(ZonedDateTime.now())} [WARN] $tag $msg\n"
            printLogToFile(log)
            println(log)
        }

        override fun error(tag: String, msg: String, t: Throwable?) {
            val log = "${formatter.format(ZonedDateTime.now())} [ERROR] $tag $msg\n"
            printLogToFile(log)
            println(log)
        }

        private fun printLogToFile(log: String) {
            logFile.appendText(log)
        }

        override fun isLoggingVerbose(): Boolean = BuildKonfig.isDebug

        override fun isLoggingDebug(): Boolean = BuildKonfig.isDebug

        override fun isLoggingInfo(): Boolean = true

        override fun isLoggingWarning(): Boolean = true

        override fun isLoggingError(): Boolean = true
    }
}
