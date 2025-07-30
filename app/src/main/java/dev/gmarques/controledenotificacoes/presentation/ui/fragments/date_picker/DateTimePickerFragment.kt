package dev.gmarques.controledenotificacoes.presentation.ui.fragments.date_picker

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.controledenotificacoes.databinding.FragmentDatetimeLayoutBinding
import dev.gmarques.controledenotificacoes.framework.FormatDateAndTimeString
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import org.joda.time.DateTime

/**
 * Criado por Gilian Marques
 * Em 29/07/2025 as 10:30
 */
@AndroidEntryPoint
class DateTimePickerFragment : MyFragment() {

    private lateinit var binding: FragmentDatetimeLayoutBinding
    private val viewModel: DateTimePickerViewModel by viewModels()

    private val args: DateTimePickerFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        return FragmentDatetimeLayoutBinding.inflate(inflater, container, false).also {
            binding = it
            setupActionBar(binding.actionbar)
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupDatePicker()
        setupTimePicker()
        setupConfirmButton()
        observeViewModel()
        viewModel.initializeDateTime(args.initialTimestamp)
    }

    /**
     * Configura o seletor de data com limites de hoje até um mês no futuro
     */
    private fun setupDatePicker() = with(binding) {
        val nowMillis = System.currentTimeMillis()
        val oneMonthFromNowMillis = DateTime(nowMillis).plusDays(30).millis

        calendarView.minDate = nowMillis
        calendarView.maxDate = oneMonthFromNowMillis

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            viewModel.updateDate(year, month, dayOfMonth)
        }
    }

    /**
     * Configura o seletor de horário com formato baseado nas preferências do sistema
     */
    private fun setupTimePicker() = with(binding) {
        timePicker.setIs24HourView(DateFormat.is24HourFormat(requireContext()))
        timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            viewModel.updateTime(hourOfDay, minute)
        }
    }

    /**
     * Configura o botão de confirmação e cancelamento
     */
    private fun setupConfirmButton() = with(binding) {
        fabConfirm.setOnClickListener {
            vibrator.interaction()
            val selectedTimestamp = viewModel.getSelectedTimestamp()
            if (selectedTimestamp != null) {
                setFragmentResult(
                    RESULT_KEY,
                    bundleOf(TIMESTAMP_KEY to selectedTimestamp)
                )
                goBack()
            }
        }
    }

    private fun observeViewModel() {
        collectFlow(viewModel.selectedDateTimeFlow) { dateTime ->
            binding.tvSelectedDateTime.text = if (dateTime != null) FormatDateAndTimeString.format(dateTime) else ""
        }

        collectFlow(viewModel.isValidSelectionFlow) { isValid ->
            binding.fabConfirm.isVisible = isValid
            binding.tvValidationMessage.isVisible = !isValid
        }
    }



    companion object {
        const val RESULT_KEY = "datetime_picker_result"
        const val TIMESTAMP_KEY = "selected_timestamp"
    }
}
