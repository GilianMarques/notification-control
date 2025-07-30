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
import dev.gmarques.controledenotificacoes.R
import dev.gmarques.controledenotificacoes.databinding.FragmentDatetimeLayoutBinding
import dev.gmarques.controledenotificacoes.presentation.ui.MyFragment
import java.util.Calendar

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
        setupSummaryCard()
        setupConfirmButton()
        observeViewModel()
        viewModel.initializeDateTime(args.initialTimestamp)
    }


    /**
     * Configura o seletor de data com limites de hoje até um mês no futuro
     */
    private fun setupDatePicker() = with(binding) {
        val now = System.currentTimeMillis()
        val oneMonthFromNow = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_MONTH, 30)
        }.timeInMillis

        calendarView.minDate = now
        calendarView.maxDate = oneMonthFromNow

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
     * Configura o card de resumo que exibe a data e hora selecionadas
     */
    private fun setupSummaryCard() {
        // Card será atualizado através do observeViewModel()
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
            updateSelectedDateTimeDisplay(dateTime)
        }

        collectFlow(viewModel.isValidSelectionFlow) { isValid ->
            binding.fabConfirm.isVisible = isValid
            binding.tvValidationMessage.isVisible = !isValid
        }
    }

    private fun updateSelectedDateTimeDisplay(dateTime: Calendar?) {
        if (dateTime == null) return

        val now = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val nextWeekStart = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 6)
        }

        val nextWeekEnd = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 12)
        }

        val timeFormat = DateFormat.getTimeFormat(requireContext())
        val formattedTime = timeFormat.format(dateTime.time)

        val displayText = when {
            // Hoje
            isSameDay(dateTime, now) -> {
                getString(R.string.Hoje_as_x, formattedTime)
            }
            // Amanhã
            isSameDay(dateTime, tomorrow) -> {
                getString(R.string.Amanha_as_x, formattedTime)
            }

            // Esta semana (depois de amanhã até próxima semana)
            dateTime.before(nextWeekStart) -> {
                val dayOfWeek = getDayOfWeekName(dateTime.get(Calendar.DAY_OF_WEEK))
                getString(R.string.x_as_x, dayOfWeek, formattedTime)
            }

            // Próxima semana
            dateTime.after(nextWeekStart) && dateTime.before(nextWeekEnd) -> {
                val dayOfWeek = getDayOfWeekName(dateTime.get(Calendar.DAY_OF_WEEK)).lowercase()
                val article = getDayOfWeekArticle(dateTime.get(Calendar.DAY_OF_WEEK))
                getString(R.string.Proximo_a_x_as_x, article, dayOfWeek, formattedTime)
            }

            // Semanas futuras
            else -> {
                val dayOfWeek = getDayOfWeekName(dateTime.get(Calendar.DAY_OF_WEEK))
                val dateFormat = DateFormat.getDateFormat(requireContext())
                val formattedDate = dateFormat.format(dateTime.time)
                getString(R.string.x_dia_x_as_x, dayOfWeek, formattedDate, formattedTime)
            }
        }

        binding.tvSelectedDateTime.text = displayText
    }

    /**
     * Verifica se duas datas são do mesmo dia
     */
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Converte o dia da semana numérico para string localizada
     */
    private fun getDayOfWeekName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> getString(R.string.Domingo)
            Calendar.MONDAY -> getString(R.string.Segunda)
            Calendar.TUESDAY -> getString(R.string.Terca)
            Calendar.WEDNESDAY -> getString(R.string.Quarta)
            Calendar.THURSDAY -> getString(R.string.Quinta)
            Calendar.FRIDAY -> getString(R.string.Sexta)
            Calendar.SATURDAY -> getString(R.string.Sabado)
            else -> ""
        }
    }

    /**
     * Retorna o artigo correto (próximo/próxima) para cada dia da semana
     */
    private fun getDayOfWeekArticle(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> getString(R.string.Proximo)
            Calendar.MONDAY -> getString(R.string.Proxima)
            Calendar.TUESDAY -> getString(R.string.Proxima)
            Calendar.WEDNESDAY -> getString(R.string.Proxima)
            Calendar.THURSDAY -> getString(R.string.Proxima)
            Calendar.FRIDAY -> getString(R.string.Proxima)
            Calendar.SATURDAY -> getString(R.string.Proximo)
            else -> ""
        }
    }

    companion object {
        const val RESULT_KEY = "datetime_picker_result"
        const val TIMESTAMP_KEY = "selected_timestamp"
    }
}