package dev.gmarques.controledenotificacoes.domain.framework

import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification

/**
 * Criado por Gilian Marques
 * Em quarta-feira, 02 de julho de 2025 as 09:32.
 */
interface Echo {

    fun repostIfNotification(activeNotification: ActiveStatusBarNotification)

}