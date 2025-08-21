/*
 * MIT License
 *
 * Copyright (c) 2025 Gilian Marques Fernandes - linkedin.com/in/gilianmarques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.example.logviewer.utils

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object JsonFormatter {

    /**
     * Tenta formatar JSON ou "pseudo JSON" (data classes).
     * Se não conseguir, retorna o texto original.
     */
    fun formatLogMessage(message: String): String {
        val trimmed = message.trim()

        // 1) Verifica se é JSON puro
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return try {
                val json = JSONObject(trimmed)
                json.toString(4) // identação de 4 espaços
            } catch (_: JSONException) {
                formatDataClassLike(trimmed)
            }
        }

        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            return try {
                val array = JSONArray(trimmed)
                array.toString(4)
            } catch (_: JSONException) {
                formatDataClassLike(trimmed)
            }
        }

        // 2) Se parece com data class estilo Kotlin: Rule(...), ActiveStatusBarNotification(...)
        if (trimmed.contains("(") && trimmed.contains(")")) {
            return formatDataClassLike(trimmed)
        }

        // 3) Caso contrário, devolve cru
        return message
    }

    /**
     * Tenta formatar uma string que parece com um objeto estilo Kotlin data class.
     *
     * Exemplo:
     * Rule(id=1c1f..., name=, days=[SATURDAY, SUNDAY], ...)
     */
    private fun formatDataClassLike(raw: String): String {
        val builder = StringBuilder()
        var indent = 0
        var current = StringBuilder()

        fun flushCurrent() {
            if (current.isNotBlank()) {
                builder.append("  ".repeat(indent))
                builder.appendLine(current.toString().trim())
                current = StringBuilder()
            }
        }

        raw.forEach { c ->
            when (c) {
                '(', '[', '{' -> {
                    current.append(c)
                    flushCurrent()
                    indent++
                }

                ')', ']', '}' -> {
                    flushCurrent()
                    indent--
                    builder.append("  ".repeat(indent)).appendLine(c.toString())
                }

                ',' -> {
                    current.append(c)
                    flushCurrent()
                }

                else -> current.append(c)
            }
        }
        flushCurrent()

        return builder.toString().trimEnd()
    }
}
