package com.snowdango.amya.platform

import kotlin.test.Test
import kotlin.test.assertEquals

class CommandLineArgsTest {

    @Test
    fun splitsOnWhitespace() {
        assertEquals(listOf("-applaunch", "613100"), CommandLineArgs.split("-applaunch 613100"))
        assertEquals(listOf("steam://rungameid/613100"), CommandLineArgs.split("steam://rungameid/613100"))
    }

    @Test
    fun repeatedAndSurroundingSpacesDoNotCreateEmptyArgs() {
        assertEquals(listOf("-a", "-b"), CommandLineArgs.split("  -a   -b  "))
        assertEquals(listOf("-a", "-b"), CommandLineArgs.split("-a\t-b"))
    }

    @Test
    fun quotedArgsKeepSpaces() {
        assertEquals(
            listOf("--config", "C:\\My Games\\a.ini", "-x"),
            CommandLineArgs.split("--config \"C:\\My Games\\a.ini\" -x"),
        )
        assertEquals(listOf("--dir=C:\\My Games"), CommandLineArgs.split("--dir=\"C:\\My Games\""))
        assertEquals(listOf("a", "", "b"), CommandLineArgs.split("a \"\" b"))
    }

    @Test
    fun emptyArgs() {
        assertEquals(emptyList(), CommandLineArgs.split(null))
        assertEquals(emptyList(), CommandLineArgs.split("   "))
    }
}
