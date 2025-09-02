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

import android.content.Context
import dev.gmarques.controledenotificacoes.R
 



/**
 * Criado por Gilian Marques
 * Em segunda-feira, 31 de março de 2025 as 23:20.
 *
 * Classe utilitária para adicionar funcionalidades ao [TimeRange]
 *
 */
object ConditionExtensionFun {


    fun Condition.description(context: Context): String {

        val maxKeywords = 3
        val hintBuilder = StringBuilder()

        hintBuilder.append(
            when (type) {
                Condition.Type.ONLY_IF -> context.getString(R.string.apenas_se)
                Condition.Type.EXCEPT -> context.getString(R.string.exceto_se)
            }
        )

        hintBuilder.append(
            when (field) {
                Condition.NotificationField.TITLE -> context.getString(R.string.o_t_tulo_da_notificacao_contiver)
                Condition.NotificationField.CONTENT -> context.getString(R.string.o_conte_do_da_notificacao_contiver)
                Condition.NotificationField.BOTH -> context.getString(R.string.o_t_tulo_ou_o_conte_do_da_notificacao_contiverem)
            }
        )

        if (keywords.size > maxKeywords) {
            keywords.take(maxKeywords).forEachIndexed { index, keyword ->
                hintBuilder.append(
                    when {
                        index + 1 < maxKeywords -> " \"$keyword\","
                        else -> " \"$keyword\"..."
                    }
                )
            }
        } else {
            keywords.forEachIndexed { index, keyword ->
                hintBuilder.append(
                    when {
                        index + 2 < keywords.size -> " \"$keyword\","
                        index + 1 < keywords.size -> " \"$keyword\""
                        keywords.size > 1 -> context.getString(R.string.ou, keyword)
                        else -> " \"$keyword\""
                    }
                )
            }
        }

        hintBuilder.append(if (keywords.isNotEmpty()) "," else " (*)")

        hintBuilder.append(
            if (caseSensitive) context.getString(R.string.considerando_letras_mai_sculas_e_min_sculas)
            else context.getString(R.string.independentemente_de_letras_mai_sculas_e_min_sculas)
        )

        return hintBuilder.toString().trim()
    }

    /**
     * Verifica se uma [AppNotification] satisfaz uma [Condition].
     *
     * A satisfação ocorre se o campo especificado na condição ([Condition.field])
     * da notificação contiver alguma das palavras-chave ([Condition.keywords]).
     * A verificação pode ser sensível a maiúsculas e minúsculas ([Condition.caseSensitive]).
     *
     * @param notification A notificação a ser verificada.
     * @return `true` se a notificação satisfizer a condição, `false` caso contrário.
     *
     * Criado por Gilian Marques
     * Em 29/06/2025 as 14:04
     */
    fun Condition.isSatisfiedBy(notification: AppNotification): Boolean {

        val textToCheck = when (field) {
            Condition.NotificationField.TITLE -> notification.title
            Condition.NotificationField.CONTENT -> notification.content
            Condition.NotificationField.BOTH -> "${notification.title} ${notification.content}"
        }

        val normalizedNotificationText = if (caseSensitive) textToCheck else textToCheck.lowercase()

        val keywords = if (caseSensitive) keywords
        else keywords.map { it.lowercase() }

        return keywords.any { keyword ->
            // É necessário isolar a palavra-chave entre espaços pra evitar falsos positivos.
            // Importantes.contains("Importante") retorna true e isso causa um comportamento indesejado.
            normalizedNotificationText.contains(" $keyword ")
        }

    }
}