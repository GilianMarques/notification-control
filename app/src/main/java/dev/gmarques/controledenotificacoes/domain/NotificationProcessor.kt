package dev.gmarques.controledenotificacoes.domain

import dev.gmarques.controledenotificacoes.domain.model.AppNotification
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 18 de julho de 2025 as 17:41.
 */
// TODO: mover para domain/framework depois de remover NotificationRuleProcessor
interface NotificationProcessor {

    fun processNotification(
        activeNotification: ActiveStatusBarNotification,
        appNotification: AppNotification,
        callback: ResultCallback,
    )

    interface ResultCallback {
        /**
         * Cancela uma notificação. Não funciona com notificações persistentes
         */
        fun cancelNotification()

        /**
         * Adia uma notificação mesmo que seja Persistente
         * Ex: Se for 60_000L a notificação sera adiada por 1 minuto antes de ser reexbida
         */
        fun snoozeNotification()

        /** app não é gerenciado, então, ignore uma notificação*/
        fun appNotManaged(activeNotification: ActiveStatusBarNotification)

        /**App em periodo de desbloqueio, permita a notificação*/
        fun allowNotification(activeNotification: ActiveStatusBarNotification)
    }
}