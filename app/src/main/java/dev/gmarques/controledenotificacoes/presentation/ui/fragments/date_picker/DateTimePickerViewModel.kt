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
