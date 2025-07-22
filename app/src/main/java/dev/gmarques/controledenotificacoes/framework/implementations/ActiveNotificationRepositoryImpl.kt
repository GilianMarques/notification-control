package dev.gmarques.controledenotificacoes.framework.implementations


import dev.gmarques.controledenotificacoes.domain.data.repository.ActiveNotificationRepository
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotification
import dev.gmarques.controledenotificacoes.framework.model.ActiveStatusBarNotificationFactory
import dev.gmarques.controledenotificacoes.framework.notification_listener_service.NotificationListener
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Em segunda-feira, 30 de junho de 2025 as 15:34.
 *
 * Usa o [NotificationListener] para obter do sistema, as notificações ativas no momento da chamada as mapeia
 * em objetos de dominio e retorna para uso na aplicação.
 */
class ActiveNotificationRepositoryImpl @Inject constructor() : ActiveNotificationRepository {

    override fun getActiveNotifications(): List<ActiveStatusBarNotification> {

        return NotificationListener.getActiveNotifications().map {
            ActiveStatusBarNotificationFactory.create(it)
        }

    }

}

