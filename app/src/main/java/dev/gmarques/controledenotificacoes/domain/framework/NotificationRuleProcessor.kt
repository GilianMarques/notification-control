package dev.gmarques.controledenotificacoes.domain.framework

import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification

/**
 * Criado por Gilian Marques
 * Em domingo, 04 de maio de 2025 as 14:10.
 *
 * Interface de contrato para a implementação de um `NotificationRuleProcessor`.
 * O `NotificationRuleProcessor` é responsável por:
 * - Verificar a existência de regras para um determinado aplicativo.
 * - Avaliar se uma notificação emitida por esse aplicativo deve ser bloqueada ou não, com base nas regras existentes.
 * - Manter um histórico das notificações de aplicativos gerenciados, utilizando os casos de uso apropriados.
 */
interface NotificationRuleProcessor {
    fun evaluateNotification(
        activeNotification: ActiveStatusBarNotification,
        appNotification: AppNotification,
        callback: Callback,
    )

    interface Callback {
        /**
         * Cancela uma notificação. Não funciona com notificações persistentes
         */
        fun onNotificationCancelled(
            activeNotification: ActiveStatusBarNotification,
            appNotification: AppNotification,
            rule: Rule,
            managedApp: ManagedApp,
        )

        /**
         * Adia uma notificação mesmo que seja Persistente
         * @param activeNotification a notificação que será adiada
         * @param snoozePeriod o tempo pelo qual a notificação ficará adiada em millisegundos.
         * Ex: Se for 60_000L a notificação sera adiada por 1 minuto antes de ser reexbida
         * */
        fun onNotificationSnoozed(activeNotification: ActiveStatusBarNotification, snoozePeriod: Long)
        fun onAppNotManaged(activeNotification: ActiveStatusBarNotification)
        fun onNotificationAllowed(activeNotification: ActiveStatusBarNotification)
    }

}