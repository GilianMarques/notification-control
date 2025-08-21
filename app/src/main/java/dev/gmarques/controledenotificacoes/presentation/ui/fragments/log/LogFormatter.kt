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

package dev.gmarques.controledenotificacoes.presentation.ui.fragments.log

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import java.util.regex.Pattern

object LogFormatter {

    fun formatLog(message: String): CharSequence {
        val sb = SpannableStringBuilder(message)

        // Regex patterns
        val patterns = listOf(
            // Classes ou objetos estilo Kotlin
            Pair(Pattern.compile("\\b[A-Z][A-Za-z0-9_]+(?=\\()"), SpanStyle(Color.parseColor("#1565C0"), Typeface.BOLD)),
            // Propriedades antes de "="
            Pair(Pattern.compile("\\b\\w+(?=\\=)"), SpanStyle(Color.parseColor("#8E24AA"), Typeface.NORMAL)),
            // Strings entre aspas
            Pair(Pattern.compile("\".*?\""), SpanStyle(Color.parseColor("#43A047"), Typeface.NORMAL)),
            // Números
            Pair(Pattern.compile("\\b\\d+\\b"), SpanStyle(Color.parseColor("#FB8C00"), Typeface.NORMAL)),
            // Boolean/null
            Pair(Pattern.compile("\\b(true|false|null)\\b"), SpanStyle(Color.parseColor("#757575"), Typeface.ITALIC))
        )

        patterns.forEach { (pattern, style) ->
            val matcher = pattern.matcher(sb)
            while (matcher.find()) {
                sb.setSpan(
                    ForegroundColorSpan(style.color),
                    matcher.start(),
                    matcher.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                sb.setSpan(
                    StyleSpan(style.typeface),
                    matcher.start(),
                    matcher.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        return sb
    }

    private data class SpanStyle(val color: Int, val typeface: Int)
}
