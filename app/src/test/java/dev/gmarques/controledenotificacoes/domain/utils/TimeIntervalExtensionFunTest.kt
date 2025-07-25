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

import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.endIntervalFormatted
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.startIntervalFormatted
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeRangeExtensionFunTest {

    @Test
    fun `start range formatted deve retornar hora e minuto com dois digitos`() {
        val casos = listOf(
            TimeRange(8, 0, 10, 30) to "08:00",
            TimeRange(12, 5, 14, 10) to "12:05",
            TimeRange(23, 59, 0, 0) to "23:59",
            TimeRange(0, 0, 6, 15) to "00:00",
            TimeRange(1, 9, 15, 30) to "01:09"
        )

        for ((range, expected) in casos) {
            assertEquals("Erro ao formatar ${range.startHour}:${range.startMinute}", expected, range.startIntervalFormatted())
        }
    }

    @Test
    fun `end range formatted deve retornar hora e minuto com dois digitos`() {
        val casos = listOf(
            TimeRange(8, 0, 10, 30) to "10:30",
            TimeRange(12, 5, 14, 10) to "14:10",
            TimeRange(23, 59, 0, 0) to "00:00",
            TimeRange(0, 0, 6, 15) to "06:15",
            TimeRange(1, 9, 15, 30) to "15:30"
        )

        for ((range, expected) in casos) {
            assertEquals("Erro ao formatar ${range.endHour}:${range.endMinute}", expected, range.endIntervalFormatted())
        }
    }
}
