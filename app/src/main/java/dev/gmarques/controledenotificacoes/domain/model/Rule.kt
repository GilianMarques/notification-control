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

package dev.gmarques.controledenotificacoes.domain.model

import androidx.annotation.Keep
import dev.gmarques.controledenotificacoes.domain.model.RuleExtensionFun.nameOrDescription
import dev.gmarques.controledenotificacoes.domain.usecase.rules.GenerateRuleDescriptionUseCase
import java.io.Serializable
import java.util.UUID

/**
 * Criado por Gilian Marques
 * Em sábado, 29 de março de 2025 às 14:39.
 *
 * Obtenha uma descrição legível dessa regra usando [RuleExtensionFun.nameOrDescription]
 * ou [GenerateRuleDescriptionUseCase] caso o nome esteja vazio
 *
 */

@Keep
data class Rule(
    val id: String = UUID.randomUUID().toString(), val name: String,
    val days: List<WeekDay>,
    val timeRanges: List<TimeRange>,
    val condition: Condition?,
    val keepFullHistory: Boolean,
    val type: Type,
    val action: Action,
) : Serializable {

    companion object {
        val typeDefault = Type.RESTRICTIVE
        val actionDefault = Action.SNOOZE
        val keepFullHistoryDefault = false
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
