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

package dev.gmarques.controledenotificacoes.domain.implementations

import android.util.Log
import dev.gmarques.controledenotificacoes.domain.framework.contracts.IncomingNotificationProcessor
import dev.gmarques.controledenotificacoes.domain.framework.contracts.IncomingNotificationProcessor.PerformAction
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.Condition
import dev.gmarques.controledenotificacoes.domain.model.ConditionExtensionFun.isSatisfiedBy
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.usecase.framework.ProcessIncomingNotificationUseCase
import dev.gmarques.controledenotificacoes.domain.usecase.rules.IsRuleInBlockPeriodUseCase
import org.joda.time.LocalDateTime
import javax.inject.Inject


/**
 * Implementação da interface [IncomingNotificationProcessor].
 *
 * Esta classe é responsável por processar notificações com base em regras definidas,
 * determinando se uma notificação deve ser permitida, bloqueada ou adiada.
 * Ela considera o tipo de regra (restritiva ou permissiva), a presença de condições
 * e se o aplicativo associado à notificação está em um período de bloqueio.
 *
 * Usada em conjunto com [ProcessIncomingNotificationUseCase]
 *

 *
 */
class IncomingNotificationProcessorImpl @Inject constructor(
    private val isRuleInBlockPeriodUseCase: IsRuleInBlockPeriodUseCase,
) : IncomingNotificationProcessor {

    /**
     * @param baseDate serve para testes. Em execução real apenas ignore.
     */
    override fun processNotification(
        appNotification: AppNotification,
        rule: Rule,
        managedApp: ManagedApp,
        baseDate: LocalDateTime,
    ): PerformAction {

        val condition = rule.condition
        val isAppInBlockPeriod = isRuleInBlockPeriodUseCase(rule, baseDate)

        return if (condition != null) processRuleWithCondition(
            isAppInBlockPeriod,
            rule,
            appNotification,
        ) else processRuleWithoutCondition(isAppInBlockPeriod, rule)

    }


    /**
     * Processa uma regra com condição, avaliando se a notificação deve ser permitida ou bloqueada.
     *
     * Esta função analisa o tipo da regra (RESTRICTIVE ou PERMISSIVE) e se o aplicativo está
     * em um período de bloqueio para determinar o comportamento.
     *
     * - Para regras RESTRICTIVE durante o período de bloqueio:
     *     - Se a condição for do tipo ONLY_IF, a notificação é permitida se a condição NÃO for satisfeita.
     *     - Se a condição for do tipo EXCEPT, a notificação é permitida se a condição FOR satisfeita.
     * - Para regras PERMISSIVE fora do período de bloqueio:
     *     - Se a condição for do tipo ONLY_IF, a notificação é permitida se a condição FOR satisfeita.
     *     - Se a condição for do tipo EXCEPT, a notificação é permitida se a condição NÃO for satisfeita.
     * - Em outros casos (por exemplo, regra RESTRICTIVE fora do período de bloqueio ou regra PERMISSIVE
     *   dentro do período de bloqueio), a notificação é permitida por padrão.
     *
     */
    private fun processRuleWithCondition(
        isAppInBlockPeriod: Boolean,
        rule: Rule,
        appNotification: AppNotification,
    ): PerformAction {
        val ruleType = rule.type
        val condition = rule.condition ?: error("Condição não pode ser nula neste ponto")

        val isConditionSatisfied = condition.isSatisfiedBy(appNotification)

        val blockNotification =
            if (ruleType == Rule.Type.RESTRICTIVE && isAppInBlockPeriod) {
                when (condition.type) {
                    Condition.Type.ONLY_IF -> isConditionSatisfied
                    Condition.Type.EXCEPT -> !isConditionSatisfied
                }

            } else if (ruleType == Rule.Type.PERMISSIVE && !isAppInBlockPeriod) {
                when (condition.type) {
                    Condition.Type.ONLY_IF -> !isConditionSatisfied
                    Condition.Type.EXCEPT -> isConditionSatisfied
                }

            } else {
                Log.w(
                    "USUK",
                    "IncomingNotificationProcessorImpl.processCondition: notificação permitida pq nao caiu em nenhuma pré-condição"
                )
                false
            }

        return if (blockNotification) decideHowToBlockNotification(rule)
        else PerformAction.Allow
    }


    /**
     * Processa uma regra sem condição, decidindo se a notificação deve ser bloqueada ou permitida.
     * Se o aplicativo estiver em período de bloqueio, chama [decideHowToBlockNotification] para determinar
     * a ação a ser tomada com base na regra. Caso contrário, permite a notificação.
     *
     * @param isAppInBlockPeriod Indica se o aplicativo está atualmente em um período de bloqueio.
     * @param rule A regra a ser processada.
     */
    private fun processRuleWithoutCondition(
        isAppInBlockPeriod: Boolean,
        rule: Rule,
    ): PerformAction {
        return if (isAppInBlockPeriod) decideHowToBlockNotification(rule)
        else PerformAction.Allow
    }

    /**
     * Decide como bloquear uma notificação com base na ação da regra
     * Esta função simplesmente mapeia a ação definida na regra ([Rule.Action.SNOOZE] ou
     * [Rule.Action.CANCEL]) para a ação correspondente a ser executada na notificação.
     * @param rule A regra que define a ação de bloqueio.
     */
    private fun decideHowToBlockNotification(rule: Rule): PerformAction {
        return when (rule.action) {
            Rule.Action.SNOOZE -> PerformAction.Snooze
            Rule.Action.CANCEL -> PerformAction.Cancel
        }
    }


}