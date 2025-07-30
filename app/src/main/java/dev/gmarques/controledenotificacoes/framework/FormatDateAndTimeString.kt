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

package dev.gmarques.controledenotificacoes.framework

import android.content.Context
import android.text.format.DateFormat
import dev.gmarques.controledenotificacoes.App
import dev.gmarques.controledenotificacoes.R
import org.joda.time.LocalDateTime

/**
 * Criado por Gilian Marques
 * em quarta-feira, 30 de julho de 2025 às 12:35.
 *
 * Objeto utilitário para formatar datas e horas em strings legíveis.
 *
 * Esta classe fornece métodos para converter um objeto [LocalDateTime] em uma representação
 * de string que é contextualmente relevante (por exemplo, "Hoje às 10:00", "Amanhã às 14:30",
 * "Segunda-feira às 09:00").
 *
 */
object FormatDateAndTimeString {

    /**
     * Formata um objeto [LocalDateTime] em uma string legível, considerando o contexto atual.
     *
     * A formatação varia dependendo se a data é hoje, amanhã, na próxima semana, etc.
     *
     * @param dateTime O objeto [LocalDateTime] a ser formatado.
     * @param context O [Context] a ser usado para obter strings localizadas e formatos de data/hora.
     *                O padrão é `App.instance`.
     * @return Uma string formatada representando a data e hora.
     */
     fun format(dateTime: LocalDateTime, context: Context = App.instance): String {

        val now = LocalDateTime.now()
        val tomorrow = now.plusDays(1)
        val nextWeekStart = now.plusDays(6)

        val timeFormat = DateFormat.getTimeFormat(context)
        val formattedTime = timeFormat.format(dateTime.toDate())

        return when {
            // Hoje
            isSameDay(dateTime, now) -> context.getString(R.string.Hoje_as_x, formattedTime)

            // Amanhã
            isSameDay(dateTime, tomorrow) -> context.getString(R.string.Amanha_as_x, formattedTime)

            // Esta semana
            dateTime.isBefore(nextWeekStart) && sundaysBetweenNowAndDate(dateTime) == 0 -> {
                val dayOfWeek = getDayOfWeekName(dateTime.dayOfWeek, context)
                context.getString(R.string.x_as_x, dayOfWeek, formattedTime)
            }

            // Próxima semana
            sundaysBetweenNowAndDate(dateTime) == 1 -> {
                val dayOfWeek = getDayOfWeekName(dateTime.dayOfWeek, context).lowercase()
                val article = getDayOfWeekArticle(dateTime.dayOfWeek, context)
                context.getString(R.string.Proximo_a_x_as_x, article, dayOfWeek, formattedTime)
            }

            // Semanas futuras
            else -> {
                val dayOfWeek = getDayOfWeekName(dateTime.dayOfWeek, context)
                val dateFormatter = DateFormat.getDateFormat(context)
                val formattedDate = dateFormatter.format(dateTime.toDate())
                context.getString(R.string.x_dia_x_as_x, dayOfWeek, formattedDate, formattedTime)
            }
        }


    }

    /**
     * Calcula o número de domingos entre a data atual e uma data alvo.
     *
     * Este mét.odo é usado para determinar se a data alvo está na "próxima semana".
     *
     * @param targetDate O objeto [LocalDateTime] da data alvo.
     * @return O número de domingos entre a data atual e a data alvo. Retorna 0 se a data alvo for anterior à data atual.
     */
    private fun sundaysBetweenNowAndDate(targetDate: LocalDateTime): Int {
        val today = LocalDateTime.now().withTime(0, 0, 0, 0)
        val end = targetDate.withTime(0, 0, 0, 0)

        if (end.isBefore(today)) return 0

        var counter = 0
        var cursor = today

        while (cursor.isBefore(end)) {
            if (cursor.dayOfWeek == 7) counter++
            cursor = cursor.plusDays(1)
        }

        return counter
    }

    /**
     * Verifica se dois objetos [LocalDateTime] representam o mesmo dia.
     *
     * @param dt1 O primeiro objeto [LocalDateTime].
     * @param dt2 O segundo objeto [LocalDateTime].
     * @return `true` se as datas forem do mesmo dia, `false` caso contrário.
     */
    private fun isSameDay(dt1: LocalDateTime, dt2: LocalDateTime): Boolean {
        return dt1.toLocalDate() == dt2.toLocalDate()
    }

    /**
     * Converte o número do dia da semana (1 para segunda-feira, 7 para domingo) em seu nome
     * localizado em string.
     *
     * @param dayOfWeek O número do dia da semana (conforme definido por Joda-Time: 1 = Segunda-feira, ..., 7 = Domingo).
     * @param context O [Context] a ser usado para obter strings localizadas.
     * @return O nome localizado do dia da semana, ou uma string vazia se o número do dia da semana
     *         for inválido.
     */
    private fun getDayOfWeekName(dayOfWeek: Int, context: Context): String {
        return when (dayOfWeek) {
            1 -> context.getString(R.string.Segunda)
            2 -> context.getString(R.string.Terca)
            3 -> context.getString(R.string.Quarta)
            4 -> context.getString(R.string.Quinta)
            5 -> context.getString(R.string.Sexta)
            6 -> context.getString(R.string.Sabado)
            7 -> context.getString(R.string.Domingo)
            else -> ""
        }
    }

    /**
     * Retorna o artigo gramatical correto ("próximo" ou "próxima") para um determinado dia da semana.
     *
     * Usado para construir frases como "Próxima segunda-feira" ou "Próximo sábado".
     *
     * @param dayOfWeek O número do dia da semana (conforme definido por Joda-Time: 1 = Segunda-feira, ..., 7 = Domingo).
     * @param context O [Context] a ser usado para obter strings localizadas.
     * @return A string "Próxima" ou "Próximo" dependendo do dia da semana, ou uma string vazia se inválido.
     */
    private fun getDayOfWeekArticle(dayOfWeek: Int, context: Context): String {
        return when (dayOfWeek) {
            1, 2, 3, 4, 5 -> context.getString(R.string.Proxima) // segunda a sexta
            6, 7 -> context.getString(R.string.Proximo) // sábado e domingo
            else -> ""
        }
    }
}