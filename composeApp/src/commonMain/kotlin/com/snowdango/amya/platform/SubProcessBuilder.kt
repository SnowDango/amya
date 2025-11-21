package com.snowdango.amya.platform

import io.matthewnelson.kmp.process.Process

expect object SubProcessBuilder {

    fun execBuilder(isRoot: Boolean, path: String, args: String?): Process.Builder
}
