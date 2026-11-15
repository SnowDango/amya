package com.snowdango.amya.steam

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class VdfParserTest {

    @Test
    fun parseLibraryFolders() {
        val vdf = VdfParser.parse(SteamFixtures.windowsLibraryFolders)
        val folders = assertNotNull(vdf.getObject("libraryfolders"))
        assertEquals(listOf("0", "1", "2"), folders.entries.map { it.first })

        val second = assertNotNull(folders.getObject("1"))
        assertEquals("F:\\SteamLibrary", second.getString("path"))
        assertEquals(listOf("613100", "5335510"), assertNotNull(second.getObject("apps")).entries.map { it.first })
        assertEquals(emptyList(), assertNotNull(folders.getObject("2")?.getObject("apps")).entries)
    }

    @Test
    fun parseAppManifest() {
        val appState = assertNotNull(VdfParser.parse(SteamFixtures.manifest(613100, "House Flipper")).getObject("AppState"))
        assertEquals("613100", appState.getString("appid"))
        assertEquals("House Flipper", appState.getString("name"))
        assertEquals("4", appState.getString("StateFlags"))
        assertEquals("english", appState.getObject("UserConfig")?.getString("language"))
    }

    @Test
    fun keysAreCaseInsensitive() {
        val vdf = VdfParser.parse(""""AppState" { "StateFlags" "4" }""")
        assertEquals("4", vdf.getObject("appstate")?.getString("stateflags"))
    }

    @Test
    fun getStringDoesNotReturnObject() {
        val vdf = VdfParser.parse(""""a" { "b" "c" }""")
        assertNull(vdf.getString("a"))
        assertNull(vdf.getObject("a")?.getObject("b"))
    }

    @Test
    fun handlesEscapesCommentsConditionalsAndUnquotedTokens() {
        val vdf = VdfParser.parse(
            """
            // comment
            root
            {
                "path"  "C:\\Games\\\"Quoted\""   // trailing comment
                "multi" "a\tb\nc"
                "cond"  "x" [${'$'}WIN32]
                unquoted value
            }
            """.trimIndent()
        )
        val root = assertNotNull(vdf.getObject("root"))
        assertEquals("C:\\Games\\\"Quoted\"", root.getString("path"))
        assertEquals("a\tb\nc", root.getString("multi"))
        assertEquals("x", root.getString("cond"))
        assertEquals("value", root.getString("unquoted"))
    }

    @Test
    fun malformedInputThrows() {
        assertFailsWith<VdfParseException> { VdfParser.parse(""""a" { "b" "c" """) }
        assertFailsWith<VdfParseException> { VdfParser.parse(""""a" "b" }""") }
        assertFailsWith<VdfParseException> { VdfParser.parse(""""a" "unclosed""") }
        assertFailsWith<VdfParseException> { VdfParser.parse(""""a"""") }
    }
}
