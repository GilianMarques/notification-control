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

package dev.gmarques.controledenotificacoes.domain.model.validators

import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeValidator
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeValidator.TimeRangeValidatorException.InversedRangeException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeRangeValidatorTest {

    @Test
    fun `deve retornar sucesso para intervalo valido`() {
        val timeRange = TimeRange(8, 0, 10, 30)
        val result = TimeRangeValidator.validate(timeRange)
        assertTrue(result.isSuccess)
        assertEquals(timeRange, result.getOrNull())
    }

    @Test
    fun `deve falhar se startHour estiver fora do intervalo`() {
        val timeRange = TimeRange(-1, 0, 10, 0)
        val result = TimeRangeValidator.validate(timeRange)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is TimeRangeValidator.TimeRangeValidatorException.HourOutOfRangeException)
    }

    @Test
    fun `deve falhar se endHour estiver fora do intervalo`() {
        val timeRange = TimeRange(8, 0, 24, 0)
        val result = TimeRangeValidator.validate(timeRange)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is TimeRangeValidator.TimeRangeValidatorException.HourOutOfRangeException)
    }

    @Test
    fun `deve falhar se startMinute estiver fora do intervalo`() {
        val timeRange = TimeRange(8, -5, 10, 0)
        val result = TimeRangeValidator.validate(timeRange)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is TimeRangeValidator.TimeRangeValidatorException.MinuteOutOfRangeException)
    }

    @Test
    fun `deve falhar se endMinute estiver fora do intervalo`() {
        val timeRange = TimeRange(8, 0, 10, 60)
        val result = TimeRangeValidator.validate(timeRange)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is TimeRangeValidator.TimeRangeValidatorException.MinuteOutOfRangeException)
    }

    @Test
    fun `deve falhar se intervalo for invertido`() {
        val timeRange = TimeRange(10, 0, 9, 30)
        val result = TimeRangeValidator.validate(timeRange)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InversedRangeException)
    }

    @Test
    fun `deve falhar se intervalo for igual`() {
        val timeRange = TimeRange(10, 0, 10, 0)
        val result = TimeRangeValidator.validate(timeRange)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InversedRangeException)
    }
}
