package dev.gmarques.controledenotificacoes.domain.framework.contracts

import dev.gmarques.controledenotificacoes.domain.model.ManagedApp
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification

/**
 * Criado por Gilian Marques
 * Em sexta-feira, 18 de julho de 2025 as 17:41.
 *
 * A implementação dessa interface é usada para processar notificações recebidas pelo sistema e determinar como trata-las de
 * acordo com as regras definidas pelo usuario. Após determinar a ação, retorna um [PerformAction] indicando a ação a ser tomada.
 */
interface IncomingNotificationProcessor {

    fun processNotification(
        activeNotification: ActiveStatusBarNotification,
        rule: Rule,
        managedApp: ManagedApp,
    ): PerformAction

    sealed class PerformAction {
        /**
         * Cancela uma notificação. Não funciona com notificações persistentes
         */
        object Cancel : PerformAction()

        /**
         * Adia uma notificação mesmo que seja Persistente
         */
        object Snooze : PerformAction()

        /**App em periodo de desbloqueio, permita a notificação*/
        object Allow : PerformAction()
    }
}