package com.snowdango.amya.platform

import io.matthewnelson.kmp.process.Process

expect object SubProcessBuilder {

    fun execBuilder(path: String): Process.Builder
}
