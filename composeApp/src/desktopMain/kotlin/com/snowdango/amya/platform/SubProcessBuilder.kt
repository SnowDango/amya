package com.snowdango.amya.platform

import io.matthewnelson.kmp.process.Process
import io.matthewnelson.kmp.process.Stdio
import io.matthewnelson.kmp.process.changeDir
import kotlin.io.path.Path

actual object SubProcessBuilder {

    actual fun execBuilder(path: String): Process.Builder {
        val parent = Path(path).parent
        return Process.Builder(path)
            .changeDir(parent.toFile())
            .stderr(Stdio.Pipe)
            .stdout(Stdio.Pipe)
            .stdin(Stdio.Pipe)
    }

}