package dev.gmarques.controledenotificacoes.domain.usecase.managed_apps

import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.isAllDayRule
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.endInMinutes
import dev.gmarques.controledenotificacoes.domain.model.TimeRangeExtensionFun.startInMinutes
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType.PERMISSIVE
import dev.gmarques.controledenotificacoes.domain.model.enums.RuleType.RESTRICTIVE
import dev.gmarques.controledenotificacoes.framework.LocalDateTimeExtFuns.weekDayNumber
import dev.gmarques.controledenotificacoes.framework.LocalDateTimeExtFuns.withSecondsAndMillisSetToZero
import org.joda.time.LocalDateTime
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 23 de maio de 2025 as 17:46.
 */
class IsAppInBlockPeriodUseCase @Inject constructor() {

    /**
     * Verifica se um aplicativo, regido por uma [Rule] específica, está atualmente em um período de bloqueio.
     * O momento da verificação é determinado pelo parâmetro [baseDate], que por padrão é o momento atual.
     *
     * Condições para um aplicativo ser considerado "em bloqueio":
     * - Se a [Rule.ruleType] for [RuleType.RESTRICTIVE]:
     *      - O dia da semana atual ([baseDate]) deve estar presente na lista [Rule.days].
     *      - A hora atual [baseDate] deve estar dentro de algum dos [dev.gmarques.controledenotificacoes.domain.model.TimeRange] definidos em [Rule.timeRanges]. Se [Rule.isAllDayRule] for `true`, esta condição é automaticamente satisfeita.
     * - Se a [Rule.ruleType] for [RuleType.PERMISSIVE]:
     *      - O dia da semana atual ([baseDate]) NÃO deve estar presente na lista [Rule.days].
     *      - OU, se o dia da semana atual estiver presente em [Rule.days], a hora atual ([baseDate]) NÃO deve estar dentro de NENHUM dos [dev.gmarques.controledenotificacoes.domain.model.TimeRange] definidos em [Rule.timeRanges]. Se [Rule.isAllDayRule] for `true` e o dia estiver presente, esta condição não será satisfeita, indicando que o app não está em bloqueio.
     *
     * @param rule A [Rule] a ser avaliada.
     * @param baseDate O [LocalDateTime] usado como referência para a verificação. Por padrão, é o momento atual.
     * @return `true` se o aplicativo estiver em um período de bloqueio de acordo com esta regra, `false` caso contrário.
     *
     * @see [Rule]
     * @see [RuleType]
     * @see [org.joda.time.LocalDateTime]
     */
    operator fun invoke(rule: Rule, baseDate: LocalDateTime = LocalDateTime()): Boolean {

        val now = LocalDateTime(baseDate).withSecondsAndMillisSetToZero()
        val currentDay = now.weekDayNumber()
        val currentMinutes = now.hourOfDay * 60 + now.minuteOfHour

        val isDayMatched = rule.days.any { it.dayNumber == currentDay }

        if (!isDayMatched) {
            // Se o dia atual não está na lista de dias da regra:
            // - Para regra PERMISSIVA: o app ESTÁ em período de bloqueio (retorna true).
            // - Para regra RESTRITIVA: o app NÃO ESTÁ em período de bloqueio (retorna false).
            return rule.ruleType == PERMISSIVE
        }

        // Se o dia atual está na lista de dias da regra:
        val isTimeMatched = if (rule.isAllDayRule()) true else rule.timeRanges.any { range ->
            currentMinutes in range.startInMinutes() until range.endInMinutes()
        }
        return when (rule.ruleType) {
            RESTRICTIVE -> isTimeMatched
            PERMISSIVE -> !isTimeMatched
        }
    }
}