package com.snowdango.amya.platform

import io.matthewnelson.kmp.process.Process
import io.matthewnelson.kmp.process.Stdio
import io.matthewnelson.kmp.process.changeDir
import kotlin.io.path.Path

actual object SubProcessBuilder {

    actual fun execBuilder(isRoot: Boolean, path: String, args: String?): Process.Builder {
        val platform = getPlatform()
        val parent = Path(path).parent
        val command = if (isRoot && platform.platformType == PlatformType.WINDOWS) {
            "cmd"
        } else {
            path
        }
        val builder = Process.Builder(command)
        if (isRoot && platform.platformType == PlatformType.WINDOWS) {
            builder.args("/c", "$path ${args ?: ""} -Verb RunAs")
        } else {
            args?.let {
                builder.args(it.split(" "))
            }
        }
        return builder
            .changeDir(parent.toFile())
            .stderr(Stdio.Pipe)
            .stdout(Stdio.Pipe)
            .stdin(Stdio.Pipe)
    }
}
