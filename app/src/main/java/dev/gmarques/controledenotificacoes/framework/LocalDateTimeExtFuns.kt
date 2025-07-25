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

import android.icu.util.Calendar
import org.joda.time.LocalDateTime

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 28 de maio de 2025 as 16:10.
 */
object LocalDateTimeExtFuns {

    /**
     * Retorna um novo [LocalDateTime] com os valores de hora e minuto definidos conforme os parâmetros.
     *
     * @param hour Hora desejada (0–23)
     * @param minute Minuto desejado (0–59)
     */
    fun LocalDateTime.at(hour: Int, minute: Int): LocalDateTime = this.withHourOfDay(hour).withMinuteOfHour(minute)

    /**
     * Zera segundos e milissegundos de um [LocalDateTime].
     * Útil para garantir consistência em comparações e testes.
     */
    fun LocalDateTime.withSecondsAndMillisSetToZero(): LocalDateTime {
        return this.withSecondOfMinute(0).withMillisOfSecond(0)
    }

    /**
     * Retorna o número do dia da semana correspondente ao [LocalDateTime],
     * utilizando a enumeração do Android `Calendar.DAY_OF_WEEK` (1 = Domingo, 7 = Sábado).
     */
    fun LocalDateTime.weekDayNumber(): Int {
        return Calendar.getInstance().apply { timeInMillis = this@weekDayNumber.toDate().time }
            .get(Calendar.DAY_OF_WEEK)
    }
}