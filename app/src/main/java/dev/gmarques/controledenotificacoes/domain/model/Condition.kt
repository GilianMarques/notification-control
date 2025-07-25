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

package dev.gmarques.controledenotificacoes.domain.model

import androidx.annotation.Keep
import java.io.Serializable

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 20 de junho de 2025 as 15:03.
 */
@Keep
data class Condition(
    val type:  Type,
    val field: NotificationField,
    val keywords: List<String>,
    val caseSensitive: Boolean = false,
) : Serializable {

    companion object {
        const val SEPARATOR = ","
    }

    /**
     * Criado por Gilian Marques
     * Em sexta-feira, 20 de junho de 2025 as 14:58.
     */
    @Keep
    enum class Type() {
        ONLY_IF, EXCEPT
    }

    /**
     * Criado por Gilian Marques
     * Em sexta-feira, 20 de junho de 2025 as 15:03.
     */
    @Keep
    enum class NotificationField() {
        TITLE, CONTENT, BOTH
    }
}