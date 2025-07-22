package dev.gmarques.controledenotificacoes.domain.data.repository

import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 30 de junho de 2025 as 15:32.
 *
 * Obtem direto do sistema android as notificações ativas no momento da chamada e converte em objetos de dominio
 * para serem usados dentro da aplicação.
 */
interface ActiveNotificationRepository {
    fun getActiveNotifications(): List<ActiveStatusBarNotification>
}