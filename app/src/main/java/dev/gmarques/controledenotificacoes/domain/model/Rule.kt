package dev.gmarques.controledenotificacoes.domain.model

import androidx.annotation.Keep
import java.io.Serializable
import java.util.UUID

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 *
 * Obtenha uma descrição legível dessa regra usando [dev.gmarques.controledenotificacoes.domain.usecase.rules.GenerateRuleDescriptionUseCase] caso o nome esteja vazio
 *
 */

@Keep
data class Rule(
    val id: String = UUID.randomUUID().toString(), val name: String,
    val days: List<WeekDay>,
    val timeRanges: List<TimeRange>,
    val condition: Condition?,
    val type: Type,
    val action: Action,
) : Serializable {

    companion object {
        val typeDefault = Type.RESTRICTIVE
        val actionDefault = Action.SNOOZE
    }

    /**Indica se a regra deve permitir ou bloquear a notificação quando as condições da regra forem satisfeitas*/
    @Keep
    enum class Type(val value: Int) {
        PERMISSIVE(1), RESTRICTIVE(0)
    }

    @Keep
    enum class WeekDay(val dayNumber: Int) { SUNDAY(1), MONDAY(2), TUESDAY(3), WEDNESDAY(4), THURSDAY(5), FRIDAY(6), SATURDAY(7), }

    /**Indica qual o comportamento da regra sobre a notificação*/
    @Keep
    enum class Action(val value: Int) {
        /**Indica que a regra deve adiar a notificação  até o proximo perido de desbloqueio do app*/
        SNOOZE(1),

        /**Indica que a regra deve cancelar a notificação*/
        CANCEL(0)
    }

}
