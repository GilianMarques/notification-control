package dev.gmarques.controledenotificacoes.presentation.ui.fragments.date_picker

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Calendar
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em 29/07/2025 as 10:30
 */
@HiltViewModel
class DateTimePickerViewModel @Inject constructor() : ViewModel() {

    private val _selectedDateTimeFlow = MutableStateFlow<Calendar?>(null)
    val selectedDateTimeFlow: Flow<Calendar?> get() = _selectedDateTimeFlow

    private val _isValidSelectionFlow = MutableStateFlow(false)
    val isValidSelectionFlow: Flow<Boolean> get() = _isValidSelectionFlow

    private var selectedCalendar: Calendar = Calendar.getInstance()

    fun initializeDateTime(initialTimestamp: Long) {
        val timestamp = initialTimestamp
        selectedCalendar.timeInMillis = timestamp

        // Garantir que não seja no passado
        val now = Calendar.getInstance()
        if (selectedCalendar.before(now)) {
            selectedCalendar = now
        }

        updateFlows()
    }

    fun updateDate(year: Int, month: Int, dayOfMonth: Int) {
        selectedCalendar.set(Calendar.YEAR, year)
        selectedCalendar.set(Calendar.MONTH, month)
        selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        updateFlows()
    }

    fun updateTime(hourOfDay: Int, minute: Int) {
        selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        selectedCalendar.set(Calendar.MINUTE, minute)
        selectedCalendar.set(Calendar.SECOND, 0)
        selectedCalendar.set(Calendar.MILLISECOND, 0)
        updateFlows()
    }

    private fun updateFlows() {
        val isValid = validateSelection()
        _selectedDateTimeFlow.tryEmit(selectedCalendar.clone() as Calendar)
        _isValidSelectionFlow.tryEmit(isValid)
    }

    private fun validateSelection(): Boolean {
        val now = Calendar.getInstance()
        val oneMonthFromNow = Calendar.getInstance().apply {
            add(Calendar.MONTH, 1)
        }

        return selectedCalendar.after(now) && selectedCalendar.before(oneMonthFromNow)
    }

    fun getSelectedTimestamp(): Long? {
        return if (_isValidSelectionFlow.value) {
            selectedCalendar.timeInMillis
        } else {
            null
        }
    }
}