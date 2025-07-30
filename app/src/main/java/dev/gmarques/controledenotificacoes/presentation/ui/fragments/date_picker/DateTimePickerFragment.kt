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
import org.joda.time.DateTime
import org.joda.time.LocalDateTime

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
            updateSelectedDateTimeDisplay(dateTime)
        }

        collectFlow(viewModel.isValidSelectionFlow) { isValid ->
            binding.fabConfirm.isVisible = isValid
            binding.tvValidationMessage.isVisible = !isValid
        }
    }

    private fun updateSelectedDateTimeDisplay(dateTime: LocalDateTime?) {
        if (dateTime == null) return

        val now = LocalDateTime.now()
        val tomorrow = now.plusDays(1)
        val nextWeekStart = now.plusDays(6)
        val nextWeekEnd = now.plusDays(12)

        val timeFormat = DateFormat.getTimeFormat(requireContext())
        val formattedTime = timeFormat.format(dateTime.toDate())

        val displayText = when {
            // Hoje
            isSameDay(dateTime, now) -> getString(R.string.Hoje_as_x, formattedTime)

            // Amanhã
            isSameDay(dateTime, tomorrow) -> getString(R.string.Amanha_as_x, formattedTime)

            // Esta semana
            dateTime.isBefore(nextWeekStart) && sundaysBetweenNowAndDate(dateTime) == 0 -> {
                val dayOfWeek = getDayOfWeekName(dateTime.dayOfWeek)
                getString(R.string.x_as_x, dayOfWeek, formattedTime)
            }

            // Próxima semana
            sundaysBetweenNowAndDate(dateTime) == 1 -> {
                val dayOfWeek = getDayOfWeekName(dateTime.dayOfWeek).lowercase()
                val article = getDayOfWeekArticle(dateTime.dayOfWeek)
                getString(R.string.Proximo_a_x_as_x, article, dayOfWeek, formattedTime)
            }

            // Semanas futuras
            else -> {
                val dayOfWeek = getDayOfWeekName(dateTime.dayOfWeek)
                val dateFormatter = DateFormat.getDateFormat(requireContext())
                val formattedDate = dateFormatter.format(dateTime.toDate())
                getString(R.string.x_dia_x_as_x, dayOfWeek, formattedDate, formattedTime)
            }
        }

        binding.tvSelectedDateTime.text = displayText
    }

    private fun sundaysBetweenNowAndDate(dataAlvo: LocalDateTime): Int {
        val hoje = LocalDateTime.now().withTime(0, 0, 0, 0)
        val fim = dataAlvo.withTime(0, 0, 0, 0)

        if (fim.isBefore(hoje)) return 0

        var contador = 0
        var cursor = hoje

        while (cursor.isBefore(fim)) {
            if (cursor.dayOfWeek == 7) contador++
            cursor = cursor.plusDays(1)
        }

        return contador
    }

    /**
     * Verifica se duas datas são do mesmo dia
     */
    private fun isSameDay(dt1: LocalDateTime, dt2: LocalDateTime): Boolean {
        return dt1.toLocalDate() == dt2.toLocalDate()
    }

    /**
     * Converte o dia da semana numérico para string localizada
     */
    private fun getDayOfWeekName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            1 -> getString(R.string.Segunda)
            2 -> getString(R.string.Terca)
            3 -> getString(R.string.Quarta)
            4 -> getString(R.string.Quinta)
            5 -> getString(R.string.Sexta)
            6 -> getString(R.string.Sabado)
            7 -> getString(R.string.Domingo)
            else -> ""
        }
    }

    /**
     * Retorna o artigo correto (próximo/próxima) para cada dia da semana
     */
    private fun getDayOfWeekArticle(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            1, 2, 3, 4, 5 -> getString(R.string.Proxima) // segunda a sexta
            6, 7 -> getString(R.string.Proximo) // sábado e domingo
            else -> ""
        }
    }

    companion object {
        const val RESULT_KEY = "datetime_picker_result"
        const val TIMESTAMP_KEY = "selected_timestamp"
    }
}
