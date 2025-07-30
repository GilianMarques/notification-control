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

package dev.gmarques.controledenotificacoes.presentation.ui.fragments.date_picker

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em 29/07/2025 as 10:30
 */
@HiltViewModel
class DateTimePickerViewModel @Inject constructor() : ViewModel() {

    private val _selectedDateTimeFlow = MutableStateFlow<LocalDateTime?>(null)
    val selectedDateTimeFlow: Flow<LocalDateTime?> get() = _selectedDateTimeFlow

    private val _isValidSelectionFlow = MutableStateFlow(false)
    val isValidSelectionFlow: Flow<Boolean> get() = _isValidSelectionFlow

    private var selectedDateTime: LocalDateTime = LocalDateTime.now()

    fun initializeDateTime(initialTimestamp: Long) {
        val initial = DateTime(initialTimestamp).toLocalDateTime()
        val now = LocalDateTime.now()

        selectedDateTime = if (initial.isBefore(now)) {
            now.plusMinutes(2)
        } else initial

        updateFlows()
    }

    fun updateDate(year: Int, month: Int, dayOfMonth: Int) {
        selectedDateTime = selectedDateTime
            .withYear(year)
            .withMonthOfYear(month + 1) // Calendar é 0-based, Joda é 1-based
            .withDayOfMonth(dayOfMonth)
        updateFlows()
    }

    fun updateTime(hourOfDay: Int, minute: Int) {
        selectedDateTime = selectedDateTime
            .withHourOfDay(hourOfDay)
            .withMinuteOfHour(minute)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0)
        updateFlows()
    }

    private fun updateFlows() {
        val isValid = validateSelection()
        _selectedDateTimeFlow.tryEmit(selectedDateTime)
        _isValidSelectionFlow.tryEmit(isValid)
    }

    private fun validateSelection(): Boolean {
        val now = LocalDateTime.now()
        val oneMonthFromNow = now.plusDays(31)
        return selectedDateTime.isAfter(now) && selectedDateTime.isBefore(oneMonthFromNow)
    }

    fun getSelectedTimestamp(): Long? {
        return if (_isValidSelectionFlow.value) {
            selectedDateTime.toDateTime().millis
        } else {
            null
        }
    }
}
