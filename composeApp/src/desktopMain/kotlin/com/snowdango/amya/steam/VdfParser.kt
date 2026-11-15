package com.snowdango.amya.steam

/**
 * Valveのテキスト形式KeyValues(VDF / ACF)のパーサ
 *
 * 対応している構文:
 * - `"key" "value"` / `"key" { ... }`
 * - クォート無しのトークン
 * - `//` コメント
 * - `[$WIN32]` のような条件指定(無視する)
 * - `\\` `\"` `\n` `\t` のエスケープ
 */
object VdfParser {

    fun parse(text: String): VdfObject {
        val tokenizer = Tokenizer(text)
        return parseObject(tokenizer, isRoot = true)
    }

    private fun parseObject(tokenizer: Tokenizer, isRoot: Boolean): VdfObject {
        val entries = mutableListOf<Pair<String, Any>>()
        while (true) {
            val keyToken = tokenizer.next()
            when (keyToken) {
                null -> {
                    if (isRoot) return VdfObject(entries)
                    throw VdfParseException("Unexpected end of input: '}' is missing")
                }

                Token.Close -> {
                    if (isRoot) throw VdfParseException("Unexpected '}'")
                    return VdfObject(entries)
                }

                Token.Open -> throw VdfParseException("Unexpected '{': key is missing")
                is Token.Str -> {
                    when (val valueToken = tokenizer.next()) {
                        Token.Open -> entries.add(keyToken.value to parseObject(tokenizer, isRoot = false))
                        is Token.Str -> entries.add(keyToken.value to valueToken.value)
                        else -> throw VdfParseException("Value is missing for key '${keyToken.value}'")
                    }
                }
            }
        }
    }

    private sealed class Token {
        data object Open : Token()
        data object Close : Token()
        data class Str(val value: String) : Token()
    }

    private class Tokenizer(private val text: String) {
        private var pos = 0

        fun next(): Token? {
            while (true) {
                skipWhitespaceAndComments()
                if (pos >= text.length) return null
                val c = text[pos]
                when {
                    c == '{' -> {
                        pos++
                        return Token.Open
                    }

                    c == '}' -> {
                        pos++
                        return Token.Close
                    }

                    c == '"' -> return Token.Str(readQuoted())
                    c == '[' -> skipConditional()
                    else -> return Token.Str(readUnquoted())
                }
            }
        }

        private fun skipWhitespaceAndComments() {
            while (pos < text.length) {
                val c = text[pos]
                if (c.isWhitespace()) {
                    pos++
                } else if (c == '/' && pos + 1 < text.length && text[pos + 1] == '/') {
                    while (pos < text.length && text[pos] != '\n') pos++
                } else {
                    return
                }
            }
        }

        private fun skipConditional() {
            val end = text.indexOf(']', pos)
            if (end < 0) throw VdfParseException("Unclosed conditional at $pos")
            pos = end + 1
        }

        private fun readQuoted(): String {
            pos++ // "
            val sb = StringBuilder()
            while (pos < text.length) {
                val c = text[pos]
                when (c) {
                    '"' -> {
                        pos++
                        return sb.toString()
                    }

                    '\\' -> {
                        if (pos + 1 >= text.length) throw VdfParseException("Invalid escape at end of input")
                        when (val escaped = text[pos + 1]) {
                            'n' -> sb.append('\n')
                            't' -> sb.append('\t')
                            '\\' -> sb.append('\\')
                            '"' -> sb.append('"')
                            else -> sb.append('\\').append(escaped)
                        }
                        pos += 2
                    }

                    else -> {
                        sb.append(c)
                        pos++
                    }
                }
            }
            throw VdfParseException("Unclosed string")
        }

        private fun readUnquoted(): String {
            val start = pos
            while (pos < text.length) {
                val c = text[pos]
                if (c.isWhitespace() || c == '{' || c == '}' || c == '"') break
                pos++
            }
            return text.substring(start, pos)
        }
    }
}

/**
 * VDFのオブジェクト。キーの重複を許し、出現順を保持する。キーの比較は大文字小文字を区別しない。
 */
class VdfObject(val entries: List<Pair<String, Any>>) {

    fun getString(key: String): String? =
        entries.firstOrNull { it.first.equals(key, ignoreCase = true) && it.second is String }?.second as String?

    fun getObject(key: String): VdfObject? =
        entries.firstOrNull { it.first.equals(key, ignoreCase = true) && it.second is VdfObject }?.second as VdfObject?
}

class VdfParseException(message: String) : Exception(message)
