package dev.gmarques.controledenotificacoes.domain.framework

import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification

/**
 * Criado por Gilian Marques
 * Em domingo, 04 de maio de 2025 as 14:10.
 *
 * Interface de contrato para a implementação de um `RuleEnforcer`.
 * O `RuleEnforcer` é responsável por:
 * - Verificar a existência de regras para um determinado aplicativo.
 * - Avaliar se uma notificação emitida por esse aplicativo deve ser bloqueada ou não, com base nas regras existentes.
 * - Manter um histórico das notificações de aplicativos gerenciados, utilizando os casos de uso apropriados.
 */
interface RuleEnforcer {
    // TODO: arrumar nome melhor  
    fun enforceOnNotification(
        activeNotification: ActiveStatusBarNotification,
        appNotification: AppNotification,
        callback: Callback,
    )

    interface Callback {
        /**
         * Cancela uma notificação. Não funciona com notificações persistentes
         */
        fun cancelNotification(activeNotification: ActiveStatusBarNotification,appNotification: AppNotification, rule: Rule, managedApp: ManagedApp)

        /**
         * Adia uma notificação mesmo que seja Persistente
         * @param sbn a notificação que será adiada
         * @param snoozePeriod o tempo pelo qual a notificaçã oficará adiada em millisegundos.
         * Ex: Se for 60_000L a notificação sera adiada por 1 minuto antes de ser reexbida
         * */
        fun snoozeNotification(activeNotification: ActiveStatusBarNotification, snoozePeriod: Long)
        fun appNotManaged(activeNotification: ActiveStatusBarNotification)
        fun allowNotification(activeNotification: ActiveStatusBarNotification)
    }

}