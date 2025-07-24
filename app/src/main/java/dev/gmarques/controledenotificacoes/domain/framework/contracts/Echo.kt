package dev.gmarques.controledenotificacoes.domain.framework.contracts

import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 02 de julho de 2025 as 09:32.
 *
 * A implementação dessa interface é usada para repostar notificações de aplicativos fora do periodo de bloqueio quando a
 * função 'Echo' esta habilitada.
 */
interface Echo {

    fun repostNotification(activeNotification: ActiveStatusBarNotification)

}