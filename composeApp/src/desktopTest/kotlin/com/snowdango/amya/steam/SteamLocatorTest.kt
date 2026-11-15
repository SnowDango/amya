package com.snowdango.amya.steam

import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SteamLocatorTest {

    @get:Rule
    val temp = TemporaryFolder()

    private fun home() = temp.newFolder("home")

    private fun steamAt(dir: File): File = SteamFixtures.createSteamRoot(dir, SteamFixtures.LibraryEntry(dir, emptyList()))

    @Test
    fun parseRegQueryOutput() {
        val output = """

            HKEY_CURRENT_USER\Software\Valve\Steam
                SteamPath    REG_SZ    c:/program files (x86)/steam

        """.trimIndent()
        assertEquals("c:/program files (x86)/steam", SteamLocator.parseRegQueryOutput(output, "SteamPath"))
        assertNull(SteamLocator.parseRegQueryOutput(output, "SteamExe"))
        assertNull(SteamLocator.parseRegQueryOutput("ERROR: The system was unable to find the specified registry key", "SteamPath"))
    }

    @Test
    fun linuxNativeInstall() {
        val home = home()
        val steam = steamAt(File(home, ".local/share/Steam"))
        val installation = assertNotNull(SteamLocator.locateLinux(home, xdgDataHome = null, pathEnv = null))
        assertEquals(steam.toPath().toRealPath().toFile(), installation.root)
    }

    @Test
    fun linuxFlatpakInstall() {
        val home = home()
        val steam = steamAt(File(home, ".var/app/com.valvesoftware.Steam/.local/share/Steam"))
        assertEquals(steam.toPath().toRealPath().toFile(), SteamLocator.locateLinux(home, null, null)?.root)
    }

    @Test
    fun linuxFlatpakDataInstall() {
        val home = home()
        val steam = steamAt(File(home, ".var/app/com.valvesoftware.Steam/data/Steam"))
        assertEquals(steam.toPath().toRealPath().toFile(), SteamLocator.locateLinux(home, null, null)?.root)
    }

    @Test
    fun linuxSnapInstall() {
        val home = home()
        val steam = steamAt(File(home, "snap/steam/common/.local/share/Steam"))
        assertEquals(steam.toPath().toRealPath().toFile(), SteamLocator.locateLinux(home, null, null)?.root)
    }

    @Test
    fun linuxXdgDataHomeHasPriority() {
        val home = home()
        steamAt(File(home, ".local/share/Steam"))
        val custom = steamAt(temp.newFolder("xdg", "Steam"))
        assertEquals(custom.toPath().toRealPath().toFile(), SteamLocator.locateLinux(home, custom.parentFile.absolutePath, null)?.root)
    }

    @Test
    fun linuxSymlinkIsResolved() {
        val home = home()
        val real = steamAt(File(home, "games/SteamReal"))
        File(home, ".steam").mkdirs()
        val link = File(home, ".steam/steam").toPath()
        val created = runCatching { Files.createSymbolicLink(link, real.toPath()) }.isSuccess
        // Windowsでは権限が無いとシンボリックリンクを作れない
        assumeTrue("symbolic link is not supported", created)
        assertEquals(real.toPath().toRealPath().toFile(), SteamLocator.locateLinux(home, null, null)?.root)
    }

    @Test
    fun linuxNotInstalled() {
        val home = home()
        // libraryfolders.vdf が無いディレクトリはSteamとみなさない
        File(home, ".local/share/Steam/steamapps").mkdirs()
        assertNull(SteamLocator.locateLinux(home, null, null))
    }

    @Test
    fun linuxLauncherUsesXdgOpenOnPath() {
        val home = home()
        steamAt(File(home, ".local/share/Steam"))
        val bin = temp.newFolder("bin")
        val xdgOpen = File(bin, "xdg-open").apply {
            writeText("#!/bin/sh\n")
            setExecutable(true)
        }
        val launcher = assertIs<SteamLauncher.Linux>(SteamLocator.locateLinux(home, null, bin.absolutePath)?.launcher)
        assertEquals(xdgOpen.absolutePath, launcher.path)
        assertEquals("steam://rungameid/613100", launcher.argsFor(613100))
    }

    @Test
    fun linuxLauncherFallsBackToUsrBin() {
        val home = home()
        steamAt(File(home, ".local/share/Steam"))
        val launcher = assertNotNull(SteamLocator.locateLinux(home, null, temp.newFolder("empty").absolutePath)).launcher
        assertEquals("/usr/bin/xdg-open", launcher.path)
    }

    @Test
    fun windowsLauncherArgs() {
        assertEquals("-applaunch 613100", SteamLauncher.Windows("C:\\Steam\\steam.exe").argsFor(613100))
    }
}
