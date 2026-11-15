package com.snowdango.amya.platform

import io.matthewnelson.kmp.process.Process
import io.matthewnelson.kmp.process.Stdio
import io.matthewnelson.kmp.process.changeDir
import java.nio.file.Path
import kotlin.io.path.Path

actual object SubProcessBuilder {

    actual fun execBuilder(isRoot: Boolean, path: String, args: String?): Process.Builder {
        val builder = if (isRoot && getPlatform().platformType == PlatformType.WINDOWS) {
            // cmd経由で起動すると、管理者権限が必要なアプリでUACの確認が表示される
            Process.Builder("cmd").args("/c", windowsCmdLine(path, args))
        } else {
            Process.Builder(path).args(CommandLineArgs.split(args))
        }
        parentDir(path)?.let { builder.changeDir(it.toFile()) }
        // 出力を読まないためPipeにしない (バッファが埋まると子プロセスが止まる・amya終了時に巻き込まれる)
        return builder
            .stdin(Stdio.Null)
            .stdout(Stdio.Null)
            .stderr(Stdio.Null)
    }

    /**
     * `cmd /c` に渡すコマンドライン。
     * cmdは括弧などの特殊文字を含むと先頭と末尾の `"` を取り除くため、全体をさらに `"` で囲む
     * (例: `""C:\Program Files (x86)\app.exe" -arg"`)
     */
    internal fun windowsCmdLine(path: String, args: String?): String {
        val command = buildString {
            append('"').append(path).append('"')
            if (!args.isNullOrBlank()) append(' ').append(args.trim())
        }
        return "\"$command\""
    }

    private fun parentDir(path: String): Path? {
        return try {
            Path(path).parent
        } catch (_: Throwable) {
            null
        }
    }
}
