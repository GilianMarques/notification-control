package dev.gmarques.controledenotificacoes.domain.implementations

import android.os.Build
import android.util.Log
import dev.gmarques.controledenotificacoes.domain.framework.contracts.IncomingNotificationProcessor
import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.AppNotificationFactory
import dev.gmarques.controledenotificacoes.domain.model.Condition
import dev.gmarques.controledenotificacoes.domain.model.ConditionExtensionFun.isSatisfiedBy
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.usecase.rules.IsRuleInBlockPeriodUseCase
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification
import javax.inject.Inject

/**
 * Implementação da interface [dev.gmarques.controledenotificacoes.domain.framework.contracts.IncomingNotificationProcessor].
 *
 * Esta classe é responsável por processar notificações com base em regras definidas,
 * determinando se uma notificação deve ser permitida, bloqueada ou adiada.
 * Ela considera o tipo de regra (restritiva ou permissiva), a presença de condições
 * e se o aplicativo associado à notificação está em um período de bloqueio.
 *
 * Usada em comjunto com [dev.gmarques.controledenotificacoes.domain.usecase.framework.ProcessIncomingNotificationUseCase]
 *
 */
class IncomingNotificationProcessorImpl @Inject constructor(
    private val isRuleInBlockPeriodUseCase: IsRuleInBlockPeriodUseCase,
) : IncomingNotificationProcessor {

    override fun processNotification(
        activeNotification: ActiveStatusBarNotification,
        rule: Rule,
        managedApp: ManagedApp,
    ): IncomingNotificationProcessor.PerformAction {

        val condition = rule.condition
        val isAppInBlockPeriod = isRuleInBlockPeriodUseCase(rule)

        return if (condition != null) processRuleWithCondition(
            isAppInBlockPeriod,
            rule,
            AppNotificationFactory.create(activeNotification),
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
    ): IncomingNotificationProcessor.PerformAction {
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
        else IncomingNotificationProcessor.PerformAction.Cancel
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
    ): IncomingNotificationProcessor.PerformAction {
        return if (isAppInBlockPeriod) decideHowToBlockNotification(rule)
        else IncomingNotificationProcessor.PerformAction.Allow
    }

    /**
     * Decide como bloquear uma notificação com base na ação da regra e na versão do SDK do Android.
     * Se a ação da regra for [Rule.Action.SNOOZE] e a versão do SDK for inferior a [android.os.Build.VERSION_CODES.O],
     * a notificação é cancelada, pois o adiamento não é suportado. Caso contrário, a ação especificada
     * na regra ([Rule.Action.SNOOZE] ou [Rule.Action.CANCEL]) é executada.
     *
     * @param rule A regra que define a ação de bloqueio.
     */
    private fun decideHowToBlockNotification(rule: Rule): IncomingNotificationProcessor.PerformAction {
        return if (rule.action == Rule.Action.SNOOZE && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            IncomingNotificationProcessor.PerformAction.Cancel // snooze isn't supported
        } else when (rule.action) {
            Rule.Action.SNOOZE -> IncomingNotificationProcessor.PerformAction.Snooze
            Rule.Action.CANCEL -> IncomingNotificationProcessor.PerformAction.Cancel
        }
    }


}