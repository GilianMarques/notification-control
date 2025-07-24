package dev.gmarques.controledenotificacoes.domain.usecase.rules

import dev.gmarques.controledenotificacoes.domain.framework.contracts.StringsProvider
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.Rule.Type
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.endInMinutes
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.startInMinutes
import org.joda.time.LocalDateTime
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 04 de abril de 2025 as 17:43.
 */
class GenerateRuleDescriptionUseCase @Inject constructor(
    private val stringsProvider: StringsProvider,
) {

    operator fun invoke(rule: Rule, baseDate: LocalDateTime = LocalDateTime.now()): String {
        val formattedDays = formatCondensedDays(rule.days)
        val range = formatTimeRanges(rule, baseDate)
        val ruleType = formatRuleType(rule.type)

        return "$ruleType $formattedDays $range"
    }

    private fun formatCondensedDays(days: List<Rule.WeekDay>): String {
        if (days.isEmpty()) return ""
        if (days.size == 7) return stringsProvider.everyDay()

        val sortedDays = days.sortedBy { it.dayNumber }

        val sequences = mutableListOf<List<Rule.WeekDay>>()
        var currentSequence = mutableListOf<Rule.WeekDay>()

        for ((index, day) in sortedDays.withIndex()) {
            if (currentSequence.isEmpty()) {
                currentSequence.add(day)
            } else {
                val lastDay = currentSequence.last()
                if (day.dayNumber == lastDay.dayNumber + 1) {
                    currentSequence.add(day)
                } else {
                    sequences.add(currentSequence)
                    currentSequence = mutableListOf(day)
                }
            }

            if (index == sortedDays.lastIndex) {
                sequences.add(currentSequence)
            }
        }

        return sequences.joinToString("/") { sequence ->
            when {
                sequence.size == 1 -> abbreviatedDay(sequence.first())
                else -> "${abbreviatedDay(sequence.first())}-${abbreviatedDay(sequence.last())}"
            }
        }
    }

    private fun abbreviatedDay(day: Rule.WeekDay): String {
        return when (day) {
            Rule.WeekDay.MONDAY -> stringsProvider.monday()
            Rule.WeekDay.TUESDAY -> stringsProvider.tuesday()
            Rule.WeekDay.WEDNESDAY -> stringsProvider.wednesday()
            Rule.WeekDay.THURSDAY -> stringsProvider.thursday()
            Rule.WeekDay.FRIDAY -> stringsProvider.friday()
            Rule.WeekDay.SATURDAY -> stringsProvider.saturday()
            Rule.WeekDay.SUNDAY -> stringsProvider.sunday()
        }
    }

    /**
     * Formata os intervalos de tempo de uma regra em uma string descritiva.
     *
     * A função recebe uma `Rule` (regra) e uma `baseDate` (data base) e retorna uma
     * representação textual dos intervalos de tempo da regra.
     *
     * A lógica é a seguinte:
     * 1.  **Validação:** Se a regra não tiver intervalos, lança um erro. Se algum intervalo for "dia inteiro",
     *     retorna a string correspondente.
     * 2.  **Intervalo Atual:** Tenta encontrar um intervalo de tempo ativo com base na `baseDate`.
     *     Os intervalos são ordenados pelo horário de início. O primeiro intervalo cujo horário de término
     *     seja igual ou posterior à hora atual é selecionado.
     * 3.  **Formatação:**
     *     *   Se um intervalo ativo for encontrado, formata seus horários de início e término no formato "HH:MM-HH:MM".
     *     *   Caso contrário (nenhum intervalo ativo no momento), encontra o intervalo com o início mais cedo
     *         e o intervalo com o término mais tarde entre todos os definidos na regra. Formata esses horários
     *         no mesmo padrão "HH:MM-HH:MM", representando o período geral coberto.
     *
     * Essencialmente, prioriza mostrar o intervalo ativo e, na ausência deste, mostra o intervalo mais amplo da regra.
     */
    private fun formatTimeRanges(rule: Rule, baseDate: LocalDateTime): String {

        if (rule.timeRanges.isEmpty()) error("uma regra deve ter pelo menos um intervalo de tempo")
        if (rule.timeRanges.any { it.allDay }) return stringsProvider.wholeDay()

        val dateBasedDesc = rule.timeRanges.apply {
            sortedBy { it.startInMinutes() }
        }.firstOrNull { it.endInMinutes() >= baseDate.hourOfDay * 60 + baseDate.minuteOfHour }

        fun formatTimes(startTime: String, endTime: String): String {
            return "%s-%s".format(startTime, endTime)
        }

        if (dateBasedDesc != null) return formatTimes(
            formatTime(dateBasedDesc.startHour, dateBasedDesc.startMinute),
            formatTime(dateBasedDesc.endHour, dateBasedDesc.endMinute)
        )

        val start = rule.timeRanges.minByOrNull { it.startInMinutes() }!!
        val end = rule.timeRanges.maxByOrNull { it.endInMinutes() }!!

        return formatTimes(
            formatTime(start.startHour, start.startMinute),
            formatTime(end.endHour, end.endMinute)
        )

    }

    private fun formatTime(hour: Int, minute: Int): String {
        return "%02d:%02d".format(hour, minute)
    }

    private fun formatRuleType(type: Type): String {
        return when (type) {
            Type.PERMISSIVE -> stringsProvider.permissive()
            Type.RESTRICTIVE -> stringsProvider.restrictive()
        }
    }
}