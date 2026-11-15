package com.snowdango.amya.platform

import io.matthewnelson.kmp.process.Stdio
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SubProcessBuilderTest {

    @get:Rule
    val temp = TemporaryFolder()

    private val isWindows = System.getProperty("os.name").startsWith("Windows")

    @Test
    fun windowsCmdLineWrapsWholeCommandInQuotes() {
        assertEquals(
            "\"\"C:\\Program Files (x86)\\Steam\\steam.exe\" -applaunch 613100\"",
            SubProcessBuilder.windowsCmdLine("C:\\Program Files (x86)\\Steam\\steam.exe", " -applaunch 613100 "),
        )
        assertEquals("\"\"C:\\a.exe\"\"", SubProcessBuilder.windowsCmdLine("C:\\a.exe", null))
    }

    /**
     * 管理者として実行 (cmd /c 経由) で、括弧とスペースを含むパスのアプリを起動できること
     */
    @Test
    fun rootLaunchOnWindowsWithParenthesesInPath() {
        assumeTrue(isWindows && getPlatform().platformType == PlatformType.WINDOWS)
        val dir = File(temp.root, "Program Files (x86)/My App").apply { mkdirs() }
        val exe = File(dir, "where.exe")
        File(System.getenv("SystemRoot"), "System32/where.exe").copyTo(exe)

        val output = SubProcessBuilder.execBuilder(isRoot = true, path = exe.absolutePath, args = "cmd")
            .stdout(Stdio.Pipe)
            .stderr(Stdio.Pipe)
            .output()
        assertNull(output.processError, output.stderr)
        assertTrue(output.stdout.contains("cmd.exe", ignoreCase = true), "stdout: ${output.stdout}, stderr: ${output.stderr}")
    }

    @Test
    fun launchFailureReturnsMessageInsteadOfThrowing() {
        val missing = File(temp.root, "not exists/app.exe").absolutePath
        val error = AppLauncher.launch(isRoot = false, path = missing, args = null)
        assertTrue(error != null && error.contains(missing))
        assertEquals("File path is empty", AppLauncher.launch(isRoot = false, path = "  ", args = null))
    }
}
