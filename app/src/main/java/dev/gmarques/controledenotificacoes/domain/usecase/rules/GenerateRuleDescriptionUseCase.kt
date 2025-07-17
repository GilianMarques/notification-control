package dev.gmarques.controledenotificacoes.domain.usecase.rules

import dev.gmarques.controledenotificacoes.domain.framework.StringsProvider
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.Rule.Type
import dev.gmarques.controledenotificacoes.domain.model.enums.WeekDay
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 04 de abril de 2025 as 17:43.
 */
class GenerateRuleDescriptionUseCase @Inject constructor(
    private val stringsProvider: StringsProvider,
) {

    operator fun invoke(rule: Rule): String {
        val formattedDays = formatCondensedDays(rule.days)
        val range = formatTimeRanges(rule)
        val ruleType = formatRuleType(rule.type)

        return "$ruleType $formattedDays $range"
    }

    private fun formatCondensedDays(days: List<WeekDay>): String {
        if (days.isEmpty()) return ""
        if (days.size == 7) return stringsProvider.everyDay()

        val sortedDays = days.sortedBy { it.dayNumber }

        val sequences = mutableListOf<List<WeekDay>>()
        var currentSequence = mutableListOf<WeekDay>()

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

    private fun abbreviatedDay(day: WeekDay): String {
        return when (day) {
            WeekDay.MONDAY -> stringsProvider.monday()
            WeekDay.TUESDAY -> stringsProvider.tuesday()
            WeekDay.WEDNESDAY -> stringsProvider.wednesday()
            WeekDay.THURSDAY -> stringsProvider.thursday()
            WeekDay.FRIDAY -> stringsProvider.friday()
            WeekDay.SATURDAY -> stringsProvider.saturday()
            WeekDay.SUNDAY -> stringsProvider.sunday()
        }
    }

    private fun formatTimeRanges(rule: Rule): String {
        if (rule.timeRanges.isEmpty()) return ""

        if (rule.timeRanges.any { it.allDay }) return stringsProvider.wholeDay()


        val start = rule.timeRanges.minByOrNull { it.startHour * 60 + it.startMinute }!!
        val end = rule.timeRanges.maxByOrNull { it.endHour * 60 + it.endMinute }!!

        val startTime = formatTime(start.startHour, start.startMinute)
        val endTime = formatTime(end.endHour, end.endMinute)

        return "%s-%s".format(startTime, endTime)

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